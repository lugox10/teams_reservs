package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.mapper.ReservationMapper;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.domain.model.*;
import com.lugo.teams.reservs.domain.repository.*;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerDashboardServiceImpl implements OwnerDashboardService {

    private final OwnerRepository ownerRepository;
    private final VenueService venueService;          // <- ahora usamos el servicio
    private final FieldRepository fieldRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationMapper reservationMapper;
    private final ReservUserRepository reservUserRepository;

    // ===================== PUBLIC API =====================

    @Override
    public List<ReservationResponseDTO> findReservationsByOwner(
            Long ownerId,
            LocalDate from,
            LocalDate to
    ) {
        if (ownerId == null) {
            throw new BadRequestException("ownerId es requerido");
        }

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        DateRange range = normalizeRange(from, to);

        // 1️⃣ venues del owner
        List<Venue> venues = venueService.findEntitiesByOwnerId(owner.getId());
        if (venues.isEmpty()) return List.of();

        List<Long> venueIds = venues.stream()
                .map(Venue::getId)
                .toList();

        // 2️⃣ buscar reservas DIRECTO por venueId + rango
        List<Reservation> reservations =
                reservationRepository.findByVenueIdInAndStartDateTimeBetween(
                        venueIds,
                        range.from.atStartOfDay(),
                        range.to.plusDays(1).atStartOfDay()
                );

        return reservationMapper.toDTOList(reservations);
    }


    @Override
    public Map<Long, Double> getMonthlyRevenueByVenue(Long ownerId, int year, int month) {
        if (ownerId == null) throw new BadRequestException("ownerId es requerido");
        if (month < 1 || month > 12) throw new BadRequestException("month inválido");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);

        List<Venue> venues = venueService.findEntitiesByOwnerId(owner.getId());
        if (venues.isEmpty()) return Map.of();

        Map<Long, Double> revenue = new HashMap<>();

        for (Venue venue : venues) {
            List<Reservation> reservations =
                    reservationRepository.findByVenue_IdInAndStatusInAndStartDateTimeBetween(
                            List.of(venue.getId()),
                            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED),
                            start.atStartOfDay(),
                            end.atStartOfDay(),
                            Pageable.unpaged() // si no necesitas paginar aquí
                    ).getContent(); // traemos lista de reservas

            BigDecimal total = calculateRevenueForReservations(reservations);
            revenue.put(venue.getId(), total.doubleValue());
        }

        return revenue;
    }


    @Override
    public Map<String, Object> getOwnerOverviewMetrics(Long ownerId, LocalDate from, LocalDate to) {
        if (ownerId == null) throw new BadRequestException("ownerId es requerido");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        DateRange range = normalizeRange(from, to);
        List<Venue> venues = venueService.findEntitiesByOwnerId(owner.getId()); // <- servicio
        List<Field> fields = venues.isEmpty()
                ? List.of()
                : venues.stream()
                .flatMap(v -> fieldRepository.findByVenueId(v.getId()).stream())
                .toList();

        List<Reservation> reservations = loadReservationsForOwnerInRange(owner, range);

        int totalReservations = reservations.size();
        BigDecimal totalRevenue = calculateRevenueForReservations(reservations);

        double occupancyRate = calculateOccupancyRate(fields, reservations, range);

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("ownerId", ownerId);
        metrics.put("from", range.from);
        metrics.put("to", range.to);
        metrics.put("totalReservations", totalReservations);
        metrics.put("totalRevenue", totalRevenue.doubleValue());
        metrics.put("occupancyRate", occupancyRate);

        return metrics;
    }

    @Override
    public Long getOwnerIdFromAuth(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            throw new BadRequestException("Authentication inválido");
        }

        String login = auth.getName().trim();
        log.debug("getOwnerIdFromAuth -> login: {}", login);

        // 1) intento directo por businessName OR name OR email (insensible a mayúsculas)
        Optional<Owner> maybeOwner = ownerRepository.findByBusinessNameOrNameOrEmail(login, login, login);
        if (maybeOwner.isPresent()) {
            return maybeOwner.get().getId();
        }

        // 2) intento por ReservUser vinculado (username/email -> ReservUser -> Owner.user)
        Optional<ReservUser> maybeUser = reservUserRepository.findByUsernameOrEmailOrIdentification(
                login, login, login
        );
        if (maybeUser.isPresent()) {
            Long userId = maybeUser.get().getId();
            Optional<Owner> ownerByUserId = ownerRepository.findByUserId(userId);
            if (ownerByUserId.isPresent()) {
                return ownerByUserId.get().getId();
            }
        }

        // 3) Si nada, error claro
        throw new NotFoundException("Owner no encontrado para businessName, name, email o usuario: " + login);
    }




    @Override
    public Page<ReservationResponseDTO> findReservationsByOwner(
            Long ownerId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado"));

        DateRange range = normalizeRange(from, to);

        List<Long> venueIds = venueService.findEntitiesByOwnerId(owner.getId())
                .stream()
                .map(Venue::getId)
                .toList();

        if (venueIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return reservationRepository
                .findByVenueIdInAndStartDateTimeBetween(
                        venueIds,
                        range.from.atStartOfDay(),
                        range.to.plusDays(1).atStartOfDay(),
                        pageable
                )
                .map(reservationMapper::toDTO);
    }




    // ================= HELPERS (sin cambios) =================

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate now = LocalDate.now();

        LocalDate effectiveFrom = (from != null) ? from : now.minusMonths(1);
        LocalDate effectiveTo = (to != null) ? to : now.plusMonths(1);

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("from no puede ser posterior a to");
        }

        return new DateRange(effectiveFrom, effectiveTo);
    }

    private List<Reservation> loadReservationsForOwnerInRange(
            Owner owner,
            DateRange range
    ) {
        List<Venue> venues = venueService.findEntitiesByOwnerId(owner.getId());
        if (venues.isEmpty()) return List.of();

        List<Long> venueIds = venues.stream()
                .map(Venue::getId)
                .toList();

        return reservationRepository.findByVenueIdInAndStartDateTimeBetween(
                venueIds,
                range.from.atStartOfDay(),
                range.to.plusDays(1).atStartOfDay()
        );
    }


    private List<Reservation> filterByRange(List<Reservation> reservations, DateRange range) {
        if (reservations == null || reservations.isEmpty()) return List.of();

        LocalDateTime fromDateTime = range.from.atStartOfDay();
        LocalDateTime toDateTime = range.to.plusDays(1).atStartOfDay();

        return reservations.stream()
                .filter(r -> {
                    LocalDateTime start = r.getStartDateTime();
                    return (start != null &&
                            !start.isBefore(fromDateTime) &&
                            start.isBefore(toDateTime));
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateRevenueForReservations(List<Reservation> reservations) {
        if (reservations == null || reservations.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = BigDecimal.ZERO;

        for (Reservation reservation : reservations) {
            BigDecimal sumForReservation = BigDecimal.ZERO;

            List<Payment> payments = paymentRepository.findByReservationId(reservation.getId());
            if (payments != null && !payments.isEmpty()) {
                for (Payment p : payments) {
                    if (p.getStatus() == PaymentStatus.PAID && !p.isRefund()) {
                        sumForReservation = sumForReservation.add(
                                p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO
                        );
                    }
                }
            } else {
                if (reservation.getPaymentStatus() == PaymentStatus.PAID &&
                        reservation.getTotalAmount() != null) {
                    sumForReservation = sumForReservation.add(reservation.getTotalAmount());
                }
            }

            total = total.add(sumForReservation);
        }

        return total;
    }

    private double calculateOccupancyRate(List<Field> fields, List<Reservation> reservations, DateRange range) {
        if (fields == null || fields.isEmpty()) return 0.0;

        long bookedMinutes = reservations.stream()
                .filter(r -> r.getStartDateTime() != null && r.getEndDateTime() != null)
                .mapToLong(r -> Duration.between(r.getStartDateTime(), r.getEndDateTime()).toMinutes())
                .sum();

        double bookedHours = bookedMinutes / 60.0;

        LocalDateTime fromDateTime = range.from.atStartOfDay();
        LocalDateTime toDateTime = range.to.plusDays(1).atStartOfDay();
        long totalMinutesRange = Duration.between(fromDateTime, toDateTime).toMinutes();
        double totalHoursRange = totalMinutesRange / 60.0;

        double totalCapacityHours = totalHoursRange * fields.size();
        if (totalCapacityHours <= 0) return 0.0;

        double occupancy = bookedHours / totalCapacityHours;
        if (occupancy < 0) occupancy = 0;
        if (occupancy > 1) occupancy = 1;

        return occupancy;
    }

    private record DateRange(LocalDate from, LocalDate to) {

    }

    @Override
    public Map<Long, Map<String, Object>> getMetricsByVenue(Long ownerId, LocalDate from, LocalDate to) {
        List<Venue> venues = venueService.findEntitiesByOwnerId(ownerId);
        if (venues == null || venues.isEmpty()) return Map.of();

        LocalDate effectiveFrom = (from != null) ? from : LocalDate.now().minusMonths(1);
        LocalDate effectiveTo = (to != null) ? to : LocalDate.now();

        // traer todas las reservas de una sola vez (por todos los venueIds)
        List<Long> venueIds = venues.stream().map(Venue::getId).toList();
        List<Reservation> allReservations = reservationRepository.findByVenueIdInAndStartDateTimeBetween(
                venueIds,
                effectiveFrom.atStartOfDay(),
                effectiveTo.plusDays(1).atStartOfDay()
        );

        Map<Long, List<Reservation>> reservationsByVenue = allReservations.stream()
                .filter(r -> r.getVenue() != null && r.getVenue().getId() != null)
                .collect(Collectors.groupingBy(r -> r.getVenue().getId()));

        Map<Long, Map<String, Object>> result = new LinkedHashMap<>();

        for (Venue v : venues) {
            Long vid = v.getId();
            List<Reservation> reservations = reservationsByVenue.getOrDefault(vid, List.of());

            int totalReservations = reservations.size();
            BigDecimal totalRevenue = calculateRevenueForReservations(reservations);

            List<Field> fields = fieldRepository.findByVenueId(vid);
            double occupancyRate = calculateOccupancyRate(fields, reservations, new DateRange(effectiveFrom, effectiveTo));

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalReservations", totalReservations);
            metrics.put("totalRevenue", totalRevenue.doubleValue());
            metrics.put("occupancyRate", occupancyRate);

            result.put(vid, metrics);
        }

        return result;
    }



}
