package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.TimeSlotDTO;
import com.lugo.teams.reservs.domain.model.TimeSlot;
import com.lugo.teams.reservs.domain.model.Field;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimeSlotMapper {

    public TimeSlotDTO toDTO(TimeSlot ts) {
        if (ts == null) return null;
        return TimeSlotDTO.builder()
                .id(ts.getId())
                .fieldId(ts.getField() != null ? ts.getField().getId() : null)
                .startDateTime(ts.getStartDateTime())
                .endDateTime(ts.getEndDateTime())
                .priceOverride(ts.getPriceOverride())
                .available(ts.isAvailable())
                .build();
    }

    public TimeSlot toEntity(TimeSlotDTO dto, Field field) {
        if (dto == null) return null;
        TimeSlot ts = new TimeSlot();
        ts.setId(dto.getId());
        ts.setField(field);
        ts.setStartDateTime(dto.getStartDateTime());
        ts.setEndDateTime(dto.getEndDateTime());
        ts.setPriceOverride(dto.getPriceOverride());
        ts.setAvailable(dto.isAvailable());
        return ts;
    }

    public List<TimeSlotDTO> toDTOList(List<TimeSlot> list) {
        if (list == null) return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
