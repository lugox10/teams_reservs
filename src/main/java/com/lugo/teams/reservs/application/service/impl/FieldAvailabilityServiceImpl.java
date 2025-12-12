// package com.lugo.teams.reservs.application.service.impl;
package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.avaliable.AvailabilityResponseDTO;
import com.lugo.teams.reservs.application.dto.avaliable.AvailabilitySlotDTO;
import com.lugo.teams.reservs.application.dto.avaliable.AvailabilityStatus;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.TimeSlot;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.PaymentOption;
import com.lugo.teams.reservs.domain.repository.FieldRepository;
import com.lugo.teams.reservs.domain.repository.TimeSlotRepository;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import com.lugo.teams.reservs.application.service.FieldAvailabilityService;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FieldAvailabilityServiceImpl implements FieldAvailabilityService {

    private final FieldRepository fieldRepository;
    private final ReservationRepository reservationRepository;
    private final TimeSlotRepository timeSlotRepository; // optional: used for priceOverride or blocked timeSlots

    @Override
    public AvailabilityResponseDTO getAvailability(Long fieldId, LocalDate date) {
        if (fieldId == null) throw new IllegalArgumentException("fieldId es requerido");
        if (date == null) throw new IllegalArgumentException("date es requerido");

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new NotFoundException("Field no encontrado: " + fieldId));

        // use venue timezone if present, else system default
        String tz = field.getVenue() != null && field.getVenue().getTimeZone() != null
                ? field.getVenue().getTimeZone()
                : ZoneId.systemDefault().getId();
        ZoneId zoneId = ZoneId.of(tz);

        int slotMinutes = Optional.ofNullable(field.getSlotMinutes()).orElse(60);
        int openHour = Optional.ofNullable(field.getOpenHour()).orElse(6);
        int closeHour = Optional.ofNullable(field.getCloseHour()).orElse(23);
        // build start/end LocalDateTime for the day (we assume times stored in same server zone / business zone)
        LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.of(openHour, 0));
        LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.of(closeHour, 0));

        // get reservations overlapping the full day window (single query)
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(fieldId, dayStart, dayEnd);

        // optionally get persisted timeSlots for that field / day (for priceOverride or blocked flags)
        LocalDateTime searchStart = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime searchEnd = LocalDateTime.of(date, LocalTime.MAX);
        List<TimeSlot> persistedSlots = timeSlotRepository.findByFieldIdAndStartDateTimeBetween(fieldId, searchStart, searchEnd);

        // build a map for quick lookup of persisted slots by start
        Map<LocalDateTime, TimeSlot> persistedByStart = persistedSlots.stream()
                .collect(Collectors.toMap(TimeSlot::getStartDateTime, ts -> ts, (a,b)->a));

        List<AvailabilitySlotDTO> slots = new ArrayList<>();

        // iterate timeslots from openHour to closeHour in steps of slotMinutes
        LocalDateTime cursor = dayStart;
        while (!cursor.isAfter(dayEnd.minusMinutes(slotMinutes))) {
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = slotStart.plusMinutes(slotMinutes);

            // status default AVAILABLE
            AvailabilityStatus status = AvailabilityStatus.AVAILABLE;
            Long reservationId = null;
            String blockedReason = null;

            // 1) check persisted timeSlot blocks/overrides
            TimeSlot ps = persistedByStart.get(slotStart);
            if (ps != null && !ps.isAvailable()) {
                status = AvailabilityStatus.BLOCKED;
                blockedReason = "Blocked by owner";
            }

            // 2) check reservations overlapping this slot
            boolean booked = overlappingReservations.stream().anyMatch(r ->
                    intervalsOverlap(slotStart, slotEnd, r.getStartDateTime(), r.getEndDateTime())
                            && r.getStatus() != null && r.getStatus().name().equals("CANCELLED") == false
            );
            if (booked) {
                status = AvailabilityStatus.BOOKED;
                // find the reservation id that overlaps (first)
                reservationId = overlappingReservations.stream()
                        .filter(r -> intervalsOverlap(slotStart, slotEnd, r.getStartDateTime(), r.getEndDateTime())
                                && r.getStatus() != null && !r.getStatus().name().equals("CANCELLED"))
                        .map(Reservation::getId)
                        .findFirst().orElse(null);
            }

            // price logic: persisted override if exists, else field.pricePerHour * slotMinutes/60
            BigDecimal price = null;
            if (ps != null && ps.getPriceOverride() != null) {
                double hours = slotMinutes / 60.0;
                price = ps.getPriceOverride().multiply(BigDecimal.valueOf(hours));
            } else if (field.getPricePerHour() != null) {
                double hours = slotMinutes / 60.0;
                price = field.getPricePerHour().multiply(BigDecimal.valueOf(hours));
            }

            // payment options derived from venue flags
            List<String> paymentOptions = new ArrayList<>();
            if (field.getVenue() != null) {
                if (field.getVenue().isAllowOnsitePayment()) paymentOptions.add("ONSITE");
                if (field.getVenue().isAllowBankTransfer()) paymentOptions.add("BANK");
                if (field.getVenue().isAllowOnlinePayment()) paymentOptions.add("ONLINE");
            }

            String label = String.format("%02d:%02d - %02d:%02d",
                    slotStart.getHour(), slotStart.getMinute(), slotEnd.getHour(), slotEnd.getMinute());

            AvailabilitySlotDTO slotDto = AvailabilitySlotDTO.builder()
                    .startDateTime(slotStart)
                    .endDateTime(slotEnd)
                    .slotLabel(label)
                    .status(status)
                    .price(price)
                    .paymentOptions(paymentOptions)
                    .reservationId(reservationId)
                    .blockedReason(blockedReason)
                    .build();

            slots.add(slotDto);
            cursor = cursor.plusMinutes(slotMinutes);
        }

        AvailabilityResponseDTO resp = AvailabilityResponseDTO.builder()
                .fieldId(field.getId())
                .date(date.toString())
                .timezone(zoneId.toString())
                .slots(slots)
                .build();

        return resp;
    }

    private boolean intervalsOverlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
}
