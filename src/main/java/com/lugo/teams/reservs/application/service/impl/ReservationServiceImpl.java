package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.teamsfc.TeamsMatchDTO;
import com.lugo.teams.reservs.application.mapper.ReservationMapper;
import com.lugo.teams.reservs.application.mapper.PaymentMapper;
import com.lugo.teams.reservs.application.mapper.TeamsMatchMapper;
import com.lugo.teams.reservs.domain.model.Payment;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationStatus;

import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.PaymentStatus;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import com.lugo.teams.reservs.domain.repository.FieldRepository;

import com.lugo.teams.reservs.domain.repository.PaymentRepository;
import com.lugo.teams.reservs.domain.repository.ReservationTeamLinkRepository;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import com.lugo.teams.reservs.application.service.ReservationService;
import com.lugo.teams.reservs.shared.exception.ConflictException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepo;
    private final FieldRepository fieldRepo;

    private final PaymentRepository paymentRepo;
    private final ReservationTeamLinkRepository reservationTeamLinkRepo;
    private final ReservUserRepository reservUserRepo;

    private final ReservationMapper reservationMapper;
    private final PaymentMapper paymentMapper;
    private final TeamsMatchMapper teamsMatchMapper;

    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher eventPublisher;

    // ---------------------- CREATE ----------------------
// imports relevantes (añádelos si tu IDE no los sugiere)
// import java.time.Duration;
// import java.time.LocalDateTime;
// import java.util.Objects;

    @Override
    @Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        if (request == null) throw new BadRequestException("request es requerido");
        if (request.getFieldId() == null) throw new BadRequestException("fieldId es requerido");

        // 1) Cargar Field y configuración básica
        Long fieldId = request.getFieldId();
        Field field = fieldRepo.findById(fieldId)
                .orElseThrow(() -> new NotFoundException("Field no encontrado: " + fieldId));

        int slotMinutes = (field.getSlotMinutes() != null) ? field.getSlotMinutes() : 60;
        int minBookingHours = (field.getMinBookingHours() != null) ? field.getMinBookingHours() : 1;
        int openHour = (field.getOpenHour() != null) ? field.getOpenHour() : 6;
        int closeHour = (field.getCloseHour() != null) ? field.getCloseHour() : 23;

        // 2) Resolver start / end según request (ya no hay timeSlot)
        LocalDateTime start = request.getStartDateTime();
        if (start == null) throw new BadRequestException("startDateTime es requerido");

        LocalDateTime end;
        Integer duration = request.getDurationMinutes();
        if (duration != null) {
            if (duration < slotMinutes) throw new BadRequestException("durationMinutes debe ser >= slotMinutes");
            if (duration % slotMinutes != 0) {
                throw new BadRequestException("durationMinutes debe ser múltiplo de slotMinutes (" + slotMinutes + ")");
            }
            end = start.plusMinutes(duration.longValue());
        } else {
            end = request.getEndDateTime();
            if (end == null) throw new BadRequestException("endDateTime es requerido si no se usa durationMinutes");
            long minutes = Duration.between(start, end).toMinutes();
            if (minutes <= 0) throw new BadRequestException("endDateTime debe ser posterior a startDateTime");
            if (minutes % slotMinutes != 0) {
                throw new BadRequestException("La duración entre start y end debe ser múltiplo de slotMinutes (" + slotMinutes + ")");
            }
            duration = (int) Duration.between(start, end).toMinutes();
        }

        // 3) Validaciones temporales básicas
        if (!start.isBefore(end)) throw new BadRequestException("startDateTime debe ser anterior a endDateTime");
        if (start.getMinute() % slotMinutes != 0 || end.getMinute() % slotMinutes != 0) {
            throw new BadRequestException("start/end deben alinearse a la granularidad del slot (" + slotMinutes + " minutos)");
        }
        if (start.getHour() < openHour || end.isAfter(LocalDateTime.of(end.toLocalDate(), LocalTime.of(closeHour, 0)))) {
            throw new BadRequestException(String.format("Reserva fuera del horario operativo del campo (open=%d, close=%d)", openHour, closeHour));
        }

        // 4) playersCount / capacity
        int playersCount = (request.getPlayersCount() == null) ? 1 : request.getPlayersCount();
        if (field.getCapacityPlayers() != null && playersCount > field.getCapacityPlayers()) {
            throw new ConflictException(String.format("playersCount (%d) excede la capacidad del campo (%d)", playersCount, field.getCapacityPlayers()));
        }

        // 5) Comprobar solapamientos en la misma cancha
        long overlapping = reservationRepo.countOverlappingReservations(fieldId, start, end);
        if (overlapping > 0) {
            meterRegistry.counter("reservation.conflict").increment();
            throw new ConflictException("Ya existe una reserva solapada en ese horario");
        }

        // 6) Regla: máximo 2 horas por día por usuario (si viene userName)
        String requestUserName = request.getUserName();
        if (requestUserName != null && !requestUserName.isBlank()) {
            LocalDate reservationDate = start.toLocalDate();
            LocalDateTime dayStart = reservationDate.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            List<Reservation> existing =
                    reservationRepo.findByUserNameOrderByStartDateTimeDesc(requestUserName)
                            .stream()
                            .filter(r ->
                                    !r.getStartDateTime().isBefore(dayStart) &&
                                            r.getStartDateTime().isBefore(dayEnd)
                            )
                            .toList();


            long existingMinutes = existing.stream()
                    .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                    .mapToLong(r -> Duration.between(r.getStartDateTime(), r.getEndDateTime()).toMinutes())
                    .sum();
            long newMinutes = Duration.between(start, end).toMinutes();
            if (existingMinutes + newMinutes > 120) {
                throw new BadRequestException("Ya tienes reservado más de 2 horas en esa fecha. Máximo permitido por día: 2 horas.");
            }
        }

        // 7) Crear entidad y calcular totalAmount (sin timeSlot)
        Reservation entity = reservationMapper.toEntity(request, field);
        entity.setStartDateTime(start);
        entity.setEndDateTime(end);
        entity.setDurationMinutes((int) Duration.between(start, end).toMinutes());
        entity.setPlayersCount(playersCount);

        if (field.getVenue() != null) {
            entity.setVenue(field.getVenue()); // ✅ seteamos el objeto completo
        }

        if (entity.getStatus() == null) entity.setStatus(ReservationStatus.PENDING);
        if (entity.getPaymentStatus() == null) entity.setPaymentStatus(PaymentStatus.NOT_INITIATED);

        // calcular totalAmount usando pricePerHour del campo
        BigDecimal total = null;
        long minutes = Duration.between(start, end).toMinutes();
        double hours = minutes / 60.0;
        if (field.getPricePerHour() != null) {
            total = field.getPricePerHour().multiply(BigDecimal.valueOf(hours));
        }
        if (total != null) entity.setTotalAmount(total);

        // vincular ReservUser o guest
        attachUserOrGuest(entity, request);

        // 8) Persistir
        Reservation saved = reservationRepo.save(entity);
        meterRegistry.counter("reservation.created").increment();

        log.info("Reserva creada id={} user={} field={} start={} end={}",
                saved.getId(),
                saved.getReservUser() != null ? saved.getReservUser().getUsername() : saved.getGuestName(),
                saved.getField().getId(), saved.getStartDateTime(), saved.getEndDateTime());

        eventPublisher.publishEvent(new ReservationCreatedEvent(this, saved.getId()));
        return reservationMapper.toDTO(saved);
    }




    @Override
    @Transactional
    public ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO dto) {
        if (id == null) throw new BadRequestException("Reservation id es requerido");
        if (dto == null) throw new BadRequestException("ReservationRequestDTO es requerido");

        Reservation existing = reservationRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + id));

        Field newField = (dto.getFieldId() != null) ?
                fieldRepo.findById(dto.getFieldId())
                        .orElseThrow(() -> new NotFoundException("Field no encontrado: " + dto.getFieldId()))
                : existing.getField();

        LocalDateTime newStart = dto.getStartDateTime() != null ? dto.getStartDateTime() : existing.getStartDateTime();

        Integer duration = dto.getDurationMinutes();
        LocalDateTime newEnd;
        if (duration != null) {
            if (duration < 1) throw new BadRequestException("durationMinutes debe ser >= 1");
            if (newStart == null) throw new BadRequestException("No hay startDateTime para aplicar durationMinutes");
            newEnd = newStart.plusMinutes(duration.longValue());
        } else if (dto.getEndDateTime() != null) {
            newEnd = dto.getEndDateTime();
        } else {
            newEnd = existing.getEndDateTime();
        }

        validateTimeBounds(newStart, newEnd);

        Integer playersCount = dto.getPlayersCount() != null ? dto.getPlayersCount() : existing.getPlayersCount();
        validateCapacity(playersCount, newField);

        List<Reservation> overlaps = reservationRepo.findOverlappingReservations(newField.getId(), newStart, newEnd);
        boolean hasConflict = overlaps.stream().anyMatch(r -> !r.getId().equals(existing.getId()));
        if (hasConflict) {
            meterRegistry.counter("reservation.conflict").increment();
            throw new ConflictException("Horario solapa con otra reserva");
        }

        // actualizar entidad vía mapper (sin TimeSlot)
        reservationMapper.updateEntityFromRequest(existing, dto, newField);

        existing.setStartDateTime(newStart);
        existing.setEndDateTime(newEnd);
        existing.setPlayersCount(playersCount);

        if (dto.getUserId() != null || dto.getGuestName() != null || dto.getGuestEmail() != null) {
            if (dto.getUserId() != null) {
                attachUserById(existing, dto.getUserId());
            } else {
                detachUserToGuestIfNeeded(existing, dto);
            }
        }

        Reservation saved = reservationRepo.save(existing);
        meterRegistry.counter("reservation.updated").increment();

        log.info("Reserva actualizada id={} user={} field={} start={} end={}",
                saved.getId(),
                saved.getReservUser() != null ? saved.getReservUser().getUsername() : saved.getGuestName(),
                saved.getField().getId(), saved.getStartDateTime(), saved.getEndDateTime());
        return reservationMapper.toDTO(saved);
    }


    // ---------------------- CONFIRM ----------------------
    @Override
    @Transactional
    public ReservationResponseDTO confirmReservation(Long reservationId, String paymentReference) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservationId));

        if (r.getStatus() == ReservationStatus.CANCELLED) throw new ConflictException("No se puede confirmar una reserva cancelada");

        if (paymentReference != null && !paymentReference.isBlank()) {
            r.setPaymentReference(paymentReference);
            r.setPaymentStatus(PaymentStatus.PAID);
        }

        r.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepo.save(r);
        meterRegistry.counter("reservation.confirmed").increment();

        log.info("Reserva confirmada id={} paymentRef={}", saved.getId(), paymentReference);
        return reservationMapper.toDTO(saved);
    }

    // ---------------------- CANCEL ----------------------
    @Override
    @Transactional
    public boolean cancelReservation(Long reservationId, String cancelledByUsername) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservationId));

        if (r.getStatus() == ReservationStatus.CANCELLED) {
            log.info("Reserva {} ya estaba cancelada", reservationId);
            return false;
        }

        r.setStatus(ReservationStatus.CANCELLED);
        reservationRepo.save(r);
        meterRegistry.counter("reservation.cancelled").increment();
        eventPublisher.publishEvent(new ReservationCancelledEvent(this, reservationId));
        log.info("Reserva {} cancelada por {}", reservationId, cancelledByUsername);
        return true;
    }

    // ---------------------- QUERIES ----------------------
    @Override
    public List<ReservationResponseDTO> findByUser(String userName) {
        if (userName == null || userName.isBlank()) throw new BadRequestException("userName es requerido");
        var list = reservationRepo.findByUserNameOrderByStartDateTimeDesc(userName);
        return list.stream().map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponseDTO> findUpcomingByVenue(Long venueId) {
        if (venueId == null) throw new BadRequestException("venueId es requerido");
        LocalDateTime from = LocalDateTime.now();
        Page<Reservation> page = reservationRepo.findByVenueIdsAndDateRange(
                List.of(venueId),
                from,
                from.plusMonths(1),
                PageRequest.of(0, 50)
        );

        List<Reservation> list = page.getContent();


        return list.stream().map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean isAvailable(Long fieldId, LocalDateTime start, LocalDateTime end) {
        if (fieldId == null) throw new BadRequestException("fieldId es requerido");
        if (start == null || end == null) throw new BadRequestException("start y end son requeridos");
        if (!start.isBefore(end)) throw new BadRequestException("start debe ser anterior a end");
        long count = reservationRepo.countOverlappingReservations(fieldId, start, end);
        return count == 0;
    }

    // ---------------------- TEAMS-FC LINK ----------------------
    @Override
    public Optional<TeamsMatchDTO> createTeamsFcMatchIfRequested(Long reservationId) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");
        return reservationTeamLinkRepo.findByReservationId(reservationId)
                .map(teamsMatchMapper::toDTO);
    }

    // ---------------------- FIND BY ID ----------------------
    @Override
    public Optional<ReservationResponseDTO> findById(Long reservationId) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");
        return reservationRepo.findById(reservationId).map(reservationMapper::toDTO);
    }

    // ---------------------- PAYMENT FLOW ----------------------
    @Override
    @Transactional
    public PaymentResultDTO initiatePayment(Long reservationId, BigDecimal amount, boolean pagoParcial) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");
        Reservation r = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservationId));

        BigDecimal toPay = (amount != null) ? amount : (r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO);
        String paymentRef = "PMT-" + UUID.randomUUID().toString();

        Payment p = new Payment();
        p.setReservation(r);
        p.setPaymentReference(paymentRef);
        p.setAmount(toPay);
        p.setStatus(PaymentStatus.PENDING);
        p.setCreatedAt(LocalDateTime.now());
        paymentRepo.save(p);

        if (!pagoParcial) {
            r.setPaymentReference(paymentRef);
            r.setPaymentStatus(PaymentStatus.PENDING);
            reservationRepo.save(r);
        }

        String checkoutUrl = "https://fake-checkout.example/checkout/" + paymentRef;
        meterRegistry.counter("payment.initiated").increment();

        return paymentMapper.toPaymentResultDto(reservationId, checkoutUrl, paymentRef, toPay, p.getStatus(), LocalDateTime.now().plusMinutes(30), "Checkout iniciado");
    }

    @Override
    @Transactional
    public void handlePaymentCallback(PaymentCallbackDTO callback) {
        if (callback == null || callback.getPaymentReference() == null) {
            log.warn("Callback inválido");
            return;
        }

        Optional<Payment> pOpt = paymentRepo.findByPaymentReference(callback.getPaymentReference());
        if (pOpt.isEmpty()) {
            log.warn("Payment no encontrado para referencia {}", callback.getPaymentReference());
            return;
        }

        Payment p = pOpt.get();
        if (p.getStatus() == callback.getPaymentStatus()) {
            log.info("Callback idempotente para payment {}", p.getId());
            return;
        }

        p.setStatus(callback.getPaymentStatus());
        if (callback.getAmount() != null) p.setAmount(callback.getAmount());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepo.save(p);

        if (p.getReservation() != null && p.getReservation().getId() != null) {
            Long resId = p.getReservation().getId();
            reservationRepo.findById(resId).ifPresent(reservation -> {
                paymentMapper.applyPaymentCallback(reservation, callback);
                reservationRepo.save(reservation);
            });
        } else {
            log.warn("Payment {} no tiene reserva asociada al procesar callback", p.getPaymentReference());
        }

        meterRegistry.counter("payment.callback.processed").increment();
        log.info("Payment callback procesado paymentRef={} status={}", p.getPaymentReference(), p.getStatus());
    }

    @Override
    @Transactional
    public boolean refundPayment(String paymentReference, BigDecimal amount) {
        if (paymentReference == null || paymentReference.isBlank()) throw new BadRequestException("paymentReference es requerido");
        Optional<Payment> pOpt = paymentRepo.findByPaymentReference(paymentReference);
        if (pOpt.isEmpty()) throw new NotFoundException("Payment no encontrado: " + paymentReference);
        Payment p = pOpt.get();

        p.setStatus(PaymentStatus.REFUNDED);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepo.save(p);

        if (p.getReservation() != null && p.getReservation().getId() != null) {
            Long resId = p.getReservation().getId();
            reservationRepo.findById(resId).ifPresent(reservation -> {
                reservation.setPaymentStatus(PaymentStatus.REFUNDED);
                reservationRepo.save(reservation);
            });
        }

        meterRegistry.counter("payment.refunded").increment();
        log.info("Payment {} reembolsado", paymentReference);
        return true;
    }

    // ---------------------- PAGINATED / LIST HELPERS ----------------------
    @Override
    public List<ReservationResponseDTO> findByUserUpcoming(String userName, LocalDateTime from, Pageable pageable) {
        if (userName == null || userName.isBlank()) throw new BadRequestException("userName es requerido");
        if (from == null) from = LocalDateTime.now();
        var list = reservationRepo.findUpcomingByUser(userName, from, pageable);
        return list.stream().map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReservationResponseDTO> findByField(Long fieldId) {
        if (fieldId == null) throw new BadRequestException("fieldId es requerido");
        var list = reservationRepo.findByFieldIdOrderByStartDateTime(fieldId);
        return list.stream().map(reservationMapper::toDTO).collect(Collectors.toList());
    }

    // ---------------------- EVENTS ----------------------
    public static class ReservationCreatedEvent {
        private final Object source;
        @Getter
        private final Long reservationId;
        public ReservationCreatedEvent(Object source, Long reservationId) { this.source = source; this.reservationId = reservationId; }
    }
    public static class ReservationCancelledEvent {
        private final Object source;
        @Getter
        private final Long reservationId;
        public ReservationCancelledEvent(Object source, Long reservationId) { this.source = source; this.reservationId = reservationId; }
    }

    // ---------------------- HELPERS ----------------------

    private void attachUserOrGuest(Reservation entity, ReservationRequestDTO request) {
        if (request.getUserId() != null) {
            attachUserById(entity, request.getUserId());
            // limpiar campos guest por si mapper los puso
            entity.setGuestName(null);
            entity.setGuestEmail(null);
            entity.setGuestPhone(null);
        } else {
            // usar datos de guest si vienen (mantener si mapper ya los puso)
            if (request.getGuestName() != null) entity.setGuestName(request.getGuestName());
            if (request.getGuestEmail() != null) entity.setGuestEmail(request.getGuestEmail());
            if (request.getGuestPhone() != null) entity.setGuestPhone(request.getGuestPhone());
            // desvincular usuario por si existía
            entity.setReservUser(null);
        }
    }

    private void attachUserById(Reservation entity, Long userId) {
        ReservUser user = reservUserRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("ReservUser no encontrado: " + userId));
        entity.setReservUser(user);
        // mantener replica de username/email en campos de Reservation para búsquedas/desnormalización
        entity.setUserName(user.getUsername());
        entity.setGuestName(null);
        entity.setGuestEmail(null);
        entity.setGuestPhone(null);
    }

    private void detachUserToGuestIfNeeded(Reservation entity, ReservationRequestDTO dto) {
        // si se quiere forzar guest: desvincular user y poner datos de guest
        entity.setReservUser(null);
        if (dto.getGuestName() != null) entity.setGuestName(dto.getGuestName());
        if (dto.getGuestEmail() != null) entity.setGuestEmail(dto.getGuestEmail());
        if (dto.getGuestPhone() != null) entity.setGuestPhone(dto.getGuestPhone());
        // opcional: limpiar userName
        entity.setUserName(null);
    }

    // Validación start < end y máximo 60 minutos
    private void validateTimeBounds(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BadRequestException("startDateTime y endDateTime son requeridos");
        }
        if (!start.isBefore(end)) {
            throw new BadRequestException("startDateTime debe ser anterior a endDateTime");
        }

        long minutes = Duration.between(start, end).toMinutes();
        if (minutes > 60) {
            throw new BadRequestException("La reserva no puede exceder los 60 minutos");
        }
    }

    // Validación de capacidad del campo
    private void validateCapacity(Integer playersCount, Field field) {
        if (field == null) throw new BadRequestException("Debe seleccionar un campo");

        int pc = (playersCount == null ? 1 : playersCount);
        if (pc < 1) throw new BadRequestException("playersCount debe ser >= 1");

        Integer cap = field.getCapacityPlayers();
        if (cap != null && pc > cap) {
            throw new ConflictException("playersCount (" + pc + ") excede capacidad del campo (" + cap + ")");
        }
    }
}
