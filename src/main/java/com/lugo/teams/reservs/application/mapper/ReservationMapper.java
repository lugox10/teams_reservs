package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationTeamLinkDTO;
import com.lugo.teams.reservs.domain.model.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReservationMapper {

    /**
     * Crea entidad Reservation a partir del request.
     * No calcula totalAmount ni gestiona pagos/links: eso lo hace el servicio.
     */
    public Reservation toEntity(ReservationRequestDTO dto, Field field) {
        if (dto == null) return null;

        Reservation r = new Reservation();
        r.setId(null);
        r.setUserName(dto.getUserName());
        r.setUserId(dto.getUserId());

        r.setField(field);
        r.setVenue(field.getVenue()); // 🔥 correcto

        r.setStartDateTime(dto.getStartDateTime());
        r.setEndDateTime(dto.getEndDateTime());

        r.setDurationMinutes(dto.getDurationMinutes());
        r.setPlayersCount(dto.getPlayersCount() != null ? dto.getPlayersCount() : 1);
        r.setTeamName(dto.getTeamName());

        r.setStatus(ReservationStatus.PENDING);
        r.setPaymentStatus(PaymentStatus.NOT_INITIATED);

        r.setNotes(dto.getNotes());

        r.setGuestName(dto.getGuestName());
        r.setGuestPhone(dto.getGuestPhone());
        r.setGuestEmail(dto.getGuestEmail());

        return r;
    }

    /**
     * Convierte entidad a DTO para respuesta.
     */
    public ReservationResponseDTO toDTO(Reservation entity) {
        if (entity == null) return null;

        ReservationResponseDTO.ReservationResponseDTOBuilder b = ReservationResponseDTO.builder()
                .id(entity.getId())
                .userName(entity.getUserName())
                .userId(entity.getUserId())
                .fieldId(entity.getField() != null ? entity.getField().getId() : null)
                .fieldName(entity.getField() != null ? entity.getField().getName() : null)
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .venueName(entity.getVenue() != null ? entity.getVenue().getName() : null)
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
            b.durationMinutes(entity.getDurationMinutes()); // fallback
        }

        // mapear links
        b.links(mapLinks(entity.getTeamLinks()));

        return b.build();
    }

    /**
     * Lista de entidades a lista de DTOs
     */
    public List<ReservationResponseDTO> toDTOList(List<Reservation> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Mapea link de equipo a DTO
     */
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

    /**
     * Actualiza entidad existente con campos del request (sin tocar IDs ni auditing)
     */
    public void updateEntityFromRequest(Reservation entity, ReservationRequestDTO dto, Field field) {
        if (entity == null || dto == null) return;

        if (dto.getFieldId() != null && field != null) {
            entity.setField(field);
            entity.setVenue(field.getVenue()); // coherencia total
        }

        if (dto.getStartDateTime() != null) entity.setStartDateTime(dto.getStartDateTime());
        if (dto.getEndDateTime() != null) entity.setEndDateTime(dto.getEndDateTime());
        if (dto.getPlayersCount() != null) entity.setPlayersCount(dto.getPlayersCount());
        if (dto.getTeamName() != null) entity.setTeamName(dto.getTeamName());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
        if (dto.getDurationMinutes() != null) entity.setDurationMinutes(dto.getDurationMinutes());

        // guest fields
        if (dto.getGuestName() != null) entity.setGuestName(dto.getGuestName());
        if (dto.getGuestPhone() != null) entity.setGuestPhone(dto.getGuestPhone());
        if (dto.getGuestEmail() != null) entity.setGuestEmail(dto.getGuestEmail());

        // NOTA: totalAmount / paymentStatus se gestionan en servicios de pago
    }
}
