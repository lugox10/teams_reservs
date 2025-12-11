package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class FieldMapper {

    public FieldDTO toDTO(Field entity) {
        if (entity == null) return null;
        return FieldDTO.builder()
                .id(entity.getId())
                .venueId(entity.getVenue() != null ? entity.getVenue().getId() : null)
                .name(entity.getName())
                .fieldType(entity.getFieldType())
                .surface(entity.getSurface())
                .capacityPlayers(entity.getCapacityPlayers())
                .pricePerHour(entity.getPricePerHour())
                .active(entity.isActive()) // autobox a Boolean
                .photos(entity.getPhotos() != null ? entity.getPhotos() : Collections.emptyList())
                .build();
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
                .build();
    }

    public List<FieldDTO> toDTOList(List<Field> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Field toEntity(FieldDTO dto, Venue venue) {
        if (dto == null) return null;
        Field f = new Field();
        f.setId(dto.getId());
        f.setName(dto.getName());
        f.setVenue(venue);
        f.setFieldType(dto.getFieldType());
        f.setSurface(dto.getSurface());
        f.setCapacityPlayers(dto.getCapacityPlayers());
        f.setPricePerHour(dto.getPricePerHour());
        // default: si no viene active, lo considero true al crear
        f.setActive(dto.getActive() != null ? dto.getActive() : true);
        f.setPhotos(dto.getPhotos() != null ? dto.getPhotos() : Collections.emptyList());
        return f;
    }
}
