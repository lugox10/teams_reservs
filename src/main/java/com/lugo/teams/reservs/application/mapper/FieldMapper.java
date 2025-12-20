 package com.lugo.teams.reservs.application.mapper;
import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.application.dto.field.FieldRequestDTO;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FieldMapper {

    public FieldDetailDTO toDetailDTO(Field entity) {
        if (entity == null) return null;
        FieldDetailDTO.FieldDetailDTOBuilder b = FieldDetailDTO.builder()
                .id(entity.getId())
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .venueName(entity.getVenue() != null ? entity.getVenue().getName() : null)
                .name(entity.getName())
                .fieldType(entity.getFieldType())
                .surface(entity.getSurface())
                .capacityPlayers(entity.getCapacityPlayers())
                .pricePerHour(entity.getPricePerHour())
                .slotMinutes(entity.getSlotMinutes())
                .openHour(entity.getOpenHour())
                .closeHour(entity.getCloseHour())
                .minBookingHours(entity.getMinBookingHours())
                .photos(entity.getPhotos() != null ? entity.getPhotos() : Collections.emptyList());

        // derive paymentOptions from venue flags (guard nulls)
        List<String> payOptions = Optional.ofNullable(entity.getVenue())
                .map(v -> {
                    List<String> p = new java.util.ArrayList<>();
                    if (v.isAllowOnsitePayment()) p.add("ONSITE");
                    if (v.isAllowBankTransfer()) p.add("BANK");
                    if (v.isAllowOnlinePayment()) p.add("ONLINE");
                    return p;
                }).orElse(Collections.emptyList());

        b.paymentOptions(payOptions);
        return b.build();
    }


    public FieldDTO toDTO(Field entity) {
        if (entity == null) return null;
        FieldDTO dto = FieldDTO.builder()
                .id(entity.getId())
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .name(entity.getName())
                .fieldType(entity.getFieldType())
                .surface(entity.getSurface())
                .capacityPlayers(entity.getCapacityPlayers())
                .pricePerHour(entity.getPricePerHour())
                .active(entity.isActive()) // autobox a Boolean
                .photos(entity.getPhotos() != null ? entity.getPhotos() : Collections.emptyList())
                // nuevos campos
                .slotMinutes(entity.getSlotMinutes())
                .openHour(entity.getOpenHour())
                .closeHour(entity.getCloseHour())
                .minBookingHours(entity.getMinBookingHours())

                .build();
        return dto;
    }

    public FieldSummaryDTO toSummary(Field entity) {
        if (entity == null) return null;
        String firstPhoto = null;
        if (entity.getPhotos() != null && !entity.getPhotos().isEmpty()) firstPhoto = entity.getPhotos().get(0);
        String venueName = Optional.ofNullable(entity.getVenue()).map(Venue::getName).orElse(null);
        return FieldSummaryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .venueName(venueName)
                .capacityPlayers(entity.getCapacityPlayers())
                .pricePerHour(entity.getPricePerHour())
                .firstPhoto(firstPhoto)
                .slotMinutes(entity.getSlotMinutes())
                .fieldType(entity.getFieldType())
                .surface(entity.getSurface())
                .build();
    }

    public List<FieldDTO> toDTOList(List<Field> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ------------------ Helpers para Requests (create / update) ------------------

    /**
     * Convierte FieldRequestDTO a entidad nueva.
     * NOTA: no setea timeSlots. La persistencia de relaciones la hace el servicio.
     */
    public Field toEntityFromRequest(FieldRequestDTO req, Venue venue) {
        if (req == null) return null;
        Field f = new Field();
        f.setName(req.getName());
        f.setVenue(venue);
        f.setFieldType(req.getFieldType());
        f.setSurface(req.getSurface());
        f.setCapacityPlayers(req.getCapacityPlayers());
        f.setPricePerHour(req.getPricePerHour());
        f.setActive(req.getActive() != null ? req.getActive() : true);
        f.setPhotos(req.getPhotos() != null ? req.getPhotos() : Collections.emptyList());

        f.setSlotMinutes(req.getSlotMinutes() != null ? req.getSlotMinutes() : 60);
        f.setOpenHour(req.getOpenHour() != null ? req.getOpenHour() : 6);
        f.setCloseHour(req.getCloseHour() != null ? req.getCloseHour() : 23);
        f.setMinBookingHours(req.getMinBookingHours() != null ? req.getMinBookingHours() : 1);

        return f;
    }

    /**
     * Actualiza una entidad existente desde el request (parcial).
     */
    public void updateEntityFromRequest(Field target, FieldRequestDTO req, Venue venue) {
        if (target == null || req == null) return;
        if (req.getName() != null) target.setName(req.getName());
        if (venue != null) target.setVenue(venue);
        if (req.getFieldType() != null) target.setFieldType(req.getFieldType());
        if (req.getSurface() != null) target.setSurface(req.getSurface());
        if (req.getCapacityPlayers() != null) target.setCapacityPlayers(req.getCapacityPlayers());
        if (req.getPricePerHour() != null) target.setPricePerHour(req.getPricePerHour());
        if (req.getActive() != null) target.setActive(req.getActive());
        if (req.getPhotos() != null) target.setPhotos(req.getPhotos());

        if (req.getSlotMinutes() != null) target.setSlotMinutes(req.getSlotMinutes());
        if (req.getOpenHour() != null) target.setOpenHour(req.getOpenHour());
        if (req.getCloseHour() != null) target.setCloseHour(req.getCloseHour());
        if (req.getMinBookingHours() != null) target.setMinBookingHours(req.getMinBookingHours());
    }
    public static Field toEntity(FieldRequestDTO dto, Venue venue) {
        Field field = new Field();

        field.setVenue(venue);
        field.setName(dto.getName());
        field.setFieldType(dto.getFieldType());
        field.setSurface(dto.getSurface());
        field.setCapacityPlayers(dto.getCapacityPlayers());
        field.setPricePerHour(dto.getPricePerHour());
        field.setSlotMinutes(dto.getSlotMinutes());
        field.setOpenHour(dto.getOpenHour());
        field.setCloseHour(dto.getCloseHour());
        field.setMinBookingHours(dto.getMinBookingHours());
        field.setActive(true);

        return field;
    }

    // ------------------ Fin helpers ------------------
}
