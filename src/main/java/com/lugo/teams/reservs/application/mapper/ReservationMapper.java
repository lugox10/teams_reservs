package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationTeamLinkDTO;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import com.lugo.teams.reservs.domain.model.TimeSlot;
import com.lugo.teams.reservs.domain.model.Field;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    public Reservation toEntity(ReservationRequestDTO dto, Field field, TimeSlot timeSlot) {
        if (dto == null) return null;
        Reservation r = new Reservation();
        r.setId(null);
        r.setUserName(dto.getUserName());
        r.setUserId(dto.getUserId());
        r.setField(field);
        r.setTimeSlot(timeSlot);
        // start/end pueden ser sobreescritos por el servicio si lo calcula con duration
        r.setStartDateTime(dto.getStartDateTime() != null ? dto.getStartDateTime() :
                (timeSlot != null ? timeSlot.getStartDateTime() : null));
        r.setEndDateTime(dto.getEndDateTime() != null ? dto.getEndDateTime() :
                (timeSlot != null ? timeSlot.getEndDateTime() : null));
        r.setPlayersCount(dto.getPlayersCount() != null ? dto.getPlayersCount() : 1);
        r.setTeamName(dto.getTeamName());
        r.setStatus(r.getStatus() == null ? com.lugo.teams.reservs.domain.model.ReservationStatus.PENDING : r.getStatus());
        r.setPaymentStatus(com.lugo.teams.reservs.domain.model.PaymentStatus.NOT_INITIATED);
        r.setTotalAmount(null); // calcular fuera del mapper
        r.setPaymentReference(null);
        r.setNotes(dto.getNotes());
        return r;
    }

    public ReservationResponseDTO toDTO(Reservation entity) {
        if (entity == null) return null;
        ReservationResponseDTO.ReservationResponseDTOBuilder b = ReservationResponseDTO.builder()
                .id(entity.getId())
                .userName(entity.getUserName())
                .userId(entity.getUserId())
                .fieldId(entity.getField() != null ? entity.getField().getId() : null)
                .fieldName(entity.getField() != null ? entity.getField().getName() : null)
                .venueId(entity.getField() != null && entity.getField().getVenue() != null ? entity.getField().getVenue().getId() : null)
                .venueName(entity.getField() != null && entity.getField().getVenue() != null ? entity.getField().getVenue().getName() : null)
                .timeSlotId(entity.getTimeSlot() != null ? entity.getTimeSlot().getId() : null)
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .playersCount(entity.getPlayersCount())
                .teamName(entity.getTeamName())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .totalAmount(entity.getTotalAmount())
                .paymentReference(entity.getPaymentReference())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        // calcular duración (minutos) si start/end están presentes
        if (entity.getStartDateTime() != null && entity.getEndDateTime() != null) {
            long mins = Duration.between(entity.getStartDateTime(), entity.getEndDateTime()).toMinutes();
            b.durationMinutes((int) mins);
        } else {
            b.durationMinutes(null);
        }

        // map links
        b.links(mapLinks(entity.getTeamLinks()));
        return b.build();
    }

    public List<ReservationResponseDTO> toDTOList(List<Reservation> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ReservationTeamLinkDTO toLinkDTO(ReservationTeamLink link) {
        if (link == null) return null;
        return ReservationTeamLinkDTO.builder()
                .id(link.getId())
                .teamsFcMatchId(link.getTeamsFcMatchId())
                .teamsFcUrl(link.getTeamsFcUrl())
                .teamName(link.getTeamName())
                .createdAt(link.getCreatedAt())
                .build();
    }

    private List<ReservationTeamLinkDTO> mapLinks(List<ReservationTeamLink> links) {
        if (links == null) return Collections.emptyList();
        return links.stream().map(this::toLinkDTO).collect(Collectors.toList());
    }

    /** Actualiza entidad existente con algunos campos de request (no sobreescribe ids ni auditing) */
    public void updateEntityFromRequest(Reservation entity, ReservationRequestDTO dto, Field field, TimeSlot timeSlot) {
        if (entity == null || dto == null) return;
        if (dto.getFieldId() != null && field != null) entity.setField(field);
        if (dto.getTimeSlotId() != null && timeSlot != null) entity.setTimeSlot(timeSlot);
        if (dto.getStartDateTime() != null) entity.setStartDateTime(dto.getStartDateTime());
        if (dto.getEndDateTime() != null) entity.setEndDateTime(dto.getEndDateTime());
        if (dto.getPlayersCount() != null) entity.setPlayersCount(dto.getPlayersCount());
        if (dto.getTeamName() != null) entity.setTeamName(dto.getTeamName());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        // NOTA: no guardamos duration en la entidad (se calcula desde start/end)
    }
}
