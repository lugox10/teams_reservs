package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.mapper.ReservationMapper;
import com.lugo.teams.reservs.application.service.OwnerDashboardService;
import com.lugo.teams.reservs.domain.model.*;
import com.lugo.teams.reservs.domain.repository.*;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final VenueRepository venueRepository;
    private final FieldRepository fieldRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationMapper reservationMapper;

    // ===================== PUBLIC API =====================

    @Override
    public List<ReservationResponseDTO> findReservationsByOwner(Long ownerId, LocalDate from, LocalDate to) {
        if (ownerId == null) throw new BadRequestException("ownerId es requerido");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        DateRange range = normalizeRange(from, to);

        List<Reservation> reservations = loadReservationsForOwnerInRange(owner, range);

        return reservationMapper.toDTOList(reservations);
    }

    @Override
    public Map<Long, Double> getMonthlyRevenueByVenue(Long ownerId, int year, int month) {
        if (ownerId == null) throw new BadRequestException("ownerId es requerido");
        if (month < 1 || month > 12) throw new BadRequestException("month debe estar entre 1 y 12");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate endExclusive = start.plusMonths(1);
        DateRange range = new DateRange(start, endExclusive.minusDays(1)); // inclusivo para cálculo interno

        List<Venue> venues = venueRepository.findByOwnerId(owner.getId());
        if (venues.isEmpty()) return Collections.emptyMap();

        // Map<venueId, BigDecimal>
        Map<Long, BigDecimal> revenueByVenue = new HashMap<>();

        for (Venue venue : venues) {
            List<Field> fields = fieldRepository.findByVenueId(venue.getId());
            if (fields.isEmpty()) {
                revenueByVenue.put(venue.getId(), BigDecimal.ZERO);
                continue;
            }

            List<Reservation> reservations = new ArrayList<>();
            for (Field field : fields) {
                List<Reservation> fieldRes =
                        reservationRepository.findByFieldIdOrderByStartDateTime(field.getId());
                reservations.addAll(filterByRange(fieldRes, range));
            }

            BigDecimal venueRevenue = calculateRevenueForReservations(reservations);
            revenueByVenue.put(venue.getId(), venueRevenue);
        }

        // Convertimos BigDecimal -> Double para respetar la firma del servicio
        return revenueByVenue.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().doubleValue()
                ));
    }

    @Override
    public Map<String, Object> getOwnerOverviewMetrics(Long ownerId, LocalDate from, LocalDate to) {
        if (ownerId == null) throw new BadRequestException("ownerId es requerido");

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + ownerId));

        DateRange range = normalizeRange(from, to);
        List<Venue> venues = venueRepository.findByOwnerId(owner.getId());
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
        metrics.put("occupancyRate", occupancyRate); // 0.0 - 1.0

        return metrics;
    }

    // ===================== HELPERS =====================

    private DateRange normalizeRange(LocalDate from, LocalDate to) {
        LocalDate now = LocalDate.now();

        LocalDate effectiveFrom = (from != null) ? from : now.minusMonths(1);
        LocalDate effectiveTo = (to != null) ? to : now.plusMonths(1);

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("from no puede ser posterior a to");
        }

        return new DateRange(effectiveFrom, effectiveTo);
    }

    private List<Reservation> loadReservationsForOwnerInRange(Owner owner, DateRange range) {
        List<Venue> venues = venueRepository.findByOwnerId(owner.getId());
        if (venues.isEmpty()) return List.of();

        List<Reservation> result = new ArrayList<>();

        for (Venue venue : venues) {
            List<Field> fields = fieldRepository.findByVenueId(venue.getId());
            for (Field field : fields) {
                List<Reservation> resByField =
                        reservationRepository.findByFieldIdOrderByStartDateTime(field.getId());
                result.addAll(filterByRange(resByField, range));
            }
        }

        return result;
    }

    /**
     * Filtra reservas por rango usando Reservation.startDateTime (incluye CANCELLED).
     */
    private List<Reservation> filterByRange(List<Reservation> reservations, DateRange range) {
        if (reservations == null || reservations.isEmpty()) return List.of();

        LocalDateTime fromDateTime = range.from.atStartOfDay();
        LocalDateTime toDateTime = range.to.plusDays(1).atStartOfDay(); // [from, to+1) para incluir el día completo

        return reservations.stream()
                .filter(r -> {
                    LocalDateTime start = r.getStartDateTime();
                    return (start != null &&
                            !start.isBefore(fromDateTime) &&
                            start.isBefore(toDateTime));
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula ingresos basados SOLO en pagos con estado PAID.
     * - Suma Payment.amount donde Payment.status == PAID y !refund.
     * - Si no hay payments pero la reserva está PAID y tiene totalAmount, usa totalAmount como fallback.
     */
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
                // fallback: usar totalAmount si la reserva está marcada como pagada
                if (reservation.getPaymentStatus() == PaymentStatus.PAID &&
                        reservation.getTotalAmount() != null) {
                    sumForReservation = sumForReservation.add(reservation.getTotalAmount());
                }
            }

            total = total.add(sumForReservation);
        }

        return total;
    }

    /**
     * Calcula una "ocupación promedio" aproximada:
     * ocupación = horas_reservadas / horas_disponibles_teóricas
     *
     * - horas_reservadas: suma de duración de todas las reservas.
     * - horas_disponibles_teóricas: (#fields del owner) * horas dentro del rango [from, to].
     *
     * Si no hay fields o el rango es raro, devuelve 0.0.
     */
    private double calculateOccupancyRate(List<Field> fields, List<Reservation> reservations, DateRange range) {
        if (fields == null || fields.isEmpty()) return 0.0;

        // horas reservadas
        long bookedMinutes = reservations.stream()
                .filter(r -> r.getStartDateTime() != null && r.getEndDateTime() != null)
                .mapToLong(r -> Duration.between(r.getStartDateTime(), r.getEndDateTime()).toMinutes())
                .sum();

        double bookedHours = bookedMinutes / 60.0;

        // horas teóricas disponibles = (#fields) * horas en rango
        LocalDateTime fromDateTime = range.from.atStartOfDay();
        LocalDateTime toDateTime = range.to.plusDays(1).atStartOfDay(); // incluir último día
        long totalMinutesRange = Duration.between(fromDateTime, toDateTime).toMinutes();
        double totalHoursRange = totalMinutesRange / 60.0;

        double totalCapacityHours = totalHoursRange * fields.size();
        if (totalCapacityHours <= 0) return 0.0;

        double occupancy = bookedHours / totalCapacityHours;
        if (occupancy < 0) occupancy = 0;
        if (occupancy > 1) occupancy = 1;

        return occupancy;
    }

    // Helper interno para manejar rangos
    private record DateRange(LocalDate from, LocalDate to) {}
}
