package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.avaliable.AvailabilityResponseDTO;
import com.lugo.teams.reservs.application.dto.avaliable.AvailabilitySlotDTO;
import com.lugo.teams.reservs.application.dto.avaliable.AvailabilityStatus;
import com.lugo.teams.reservs.application.service.FieldAvailabilityService;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.Venue;
import com.lugo.teams.reservs.domain.repository.FieldRepository;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FieldAvailabilityServiceImpl implements FieldAvailabilityService {

    private final FieldRepository fieldRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public AvailabilityResponseDTO getAvailability(Long fieldId, LocalDate date) {

        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("Cancha no encontrada"));

        List<Reservation> reservations =
                reservationRepository.findByFieldIdOrderByStartDateTime(fieldId);

        ZoneId zone = ZoneId.systemDefault();

        List<AvailabilitySlotDTO> slots = new ArrayList<>();

        LocalTime cursor = LocalTime.MIDNIGHT;
        LocalTime endDay = LocalTime.of(23, 59);

        while (cursor.isBefore(endDay)) {

            LocalTime next = cursor.plusMinutes(field.getSlotMinutes());

            LocalDateTime startDT = LocalDateTime.of(date, cursor);
            LocalDateTime endDT = LocalDateTime.of(date, next);

            AvailabilityStatus status;
            Long reservationId = null;
            String blockedReason = null;

            if (cursor.getHour() < field.getOpenHour()
                    || next.getHour() > field.getCloseHour()) {

                status = AvailabilityStatus.BLOCKED;
                blockedReason = "Fuera de horario";

            } else {

                Optional<Reservation> overlap =
                        reservations.stream()
                                .filter(r ->
                                        r.getStartDateTime().isBefore(endDT) &&
                                                r.getEndDateTime().isAfter(startDT))
                                .findFirst();

                if (overlap.isPresent()) {
                    status = AvailabilityStatus.BOOKED;
                    reservationId = overlap.get().getId();
                } else {
                    status = AvailabilityStatus.AVAILABLE;
                }
            }

            slots.add(AvailabilitySlotDTO.builder()
                    .startDateTime(startDT)
                    .endDateTime(endDT)
                    .slotLabel(cursor + " - " + next)
                    .status(status)
                    .price(field.getPricePerHour())
                    .paymentOptions(resolvePaymentOptions(field.getVenue()))
                    .reservationId(reservationId)
                    .blockedReason(blockedReason)
                    .build());

            cursor = next;
        }

        return AvailabilityResponseDTO.builder()
                .fieldId(fieldId)
                .date(date.toString())
                .timezone(zone.getId())
                .slots(slots)
                .build();
    }

    private List<String> resolvePaymentOptions(Venue venue) {
        List<String> options = new ArrayList<>();
        if (venue.isAllowOnsitePayment()) options.add("ONSITE");
        if (venue.isAllowBankTransfer()) options.add("TRANSFER");
        if (venue.isAllowOnlinePayment()) options.add("ONLINE");
        return options;
    }
}
