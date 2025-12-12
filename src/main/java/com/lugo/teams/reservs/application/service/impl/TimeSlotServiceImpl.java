package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.slot.TimeSlotDTO;

import com.lugo.teams.reservs.application.mapper.TimeSlotMapper;
import com.lugo.teams.reservs.application.service.TimeSlotService;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.TimeSlot;
import com.lugo.teams.reservs.domain.repository.FieldRepository;
import com.lugo.teams.reservs.domain.repository.TimeSlotRepository;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.ConflictException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final FieldRepository fieldRepository;
    private final TimeSlotMapper mapper;
    private final MeterRegistry meterRegistry;

    // ======================= CREATE =======================
    @Override
    @Transactional
    public TimeSlotDTO createTimeSlot(TimeSlotDTO dto) {
        validateDTO(dto);

        Field field = fieldRepository.findById(dto.getFieldId())
                .orElseThrow(() -> new NotFoundException("Field no encontrado: " + dto.getFieldId()));

        validateDateRange(dto.getStartDateTime(), dto.getEndDateTime());
        validateNoOverlap(field.getId(), dto.getStartDateTime(), dto.getEndDateTime(), null);

        TimeSlot entity = mapper.toEntity(dto, field);
        // si quieres forzar disponibles al crear, puedes hacer:
        // entity.setAvailable(true);

        TimeSlot saved = timeSlotRepository.save(entity);
        meterRegistry.counter("timeslot.created").increment();

        log.info("TimeSlot creado id={} fieldId={} {} -> {}",
                saved.getId(),
                field.getId(),
                saved.getStartDateTime(),
                saved.getEndDateTime());

        return mapper.toDTO(saved);
    }

    // ======================= UPDATE =======================
    @Override
    @Transactional
    public TimeSlotDTO updateTimeSlot(Long id, TimeSlotDTO dto) {
        if (id == null) throw new BadRequestException("id es requerido");
        validateDTO(dto);

        TimeSlot existing = timeSlotRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("TimeSlot no encontrado: " + id));

        // si permiten cambiar de cancha en el update:
        Field field = existing.getField();
        if (dto.getFieldId() != null &&
                (field == null || !dto.getFieldId().equals(field.getId()))) {
            field = fieldRepository.findById(dto.getFieldId())
                    .orElseThrow(() -> new NotFoundException("Field no encontrado: " + dto.getFieldId()));
            existing.setField(field);
        }

        validateDateRange(dto.getStartDateTime(), dto.getEndDateTime());
        validateNoOverlap(existing.getField().getId(), dto.getStartDateTime(), dto.getEndDateTime(), id);

        // actualizar campos simples
        existing.setStartDateTime(dto.getStartDateTime());
        existing.setEndDateTime(dto.getEndDateTime());
        existing.setPriceOverride(dto.getPriceOverride());

        // <-- CORRECCIÓN: usar getAvailable() y no sobrescribir si viene null
        if (dto.getAvailable() != null) {
            existing.setAvailable(dto.getAvailable());
        }

        TimeSlot saved = timeSlotRepository.save(existing);
        meterRegistry.counter("timeslot.updated").increment();

        log.info("TimeSlot actualizado id={} fieldId={} {} -> {} available={}",
                saved.getId(),
                saved.getField() != null ? saved.getField().getId() : null,
                saved.getStartDateTime(),
                saved.getEndDateTime(),
                saved.isAvailable());

        return mapper.toDTO(saved);
    }


    // ======================= FIND BY ID =======================
    @Override
    public Optional<TimeSlotDTO> findById(Long id) {
        if (id == null) throw new BadRequestException("id es requerido");
        return timeSlotRepository.findById(id).map(mapper::toDTO);
    }

    // ======================= FIND AVAILABLE BETWEEN =======================
    @Override
    public List<TimeSlotDTO> findAvailableByFieldBetween(Long fieldId, LocalDateTime from, LocalDateTime to) {
        if (fieldId == null) throw new BadRequestException("fieldId es requerido");
        validateDateRange(from, to);

        var list = timeSlotRepository.findAvailableByFieldBetween(fieldId, from, to);
        return mapper.toDTOList(list);
    }

    // ======================= FIND BY FIELD =======================
    @Override
    public List<TimeSlotDTO> findByFieldId(Long fieldId) {
        if (fieldId == null) throw new BadRequestException("fieldId es requerido");

        // tu repo solo tiene findByFieldIdAndAvailableTrue -> devolvemos los disponibles
        var list = timeSlotRepository.findByFieldIdAndAvailableTrue(fieldId);
        return mapper.toDTOList(list);
    }

    // ======================= SET AVAILABILITY =======================
    @Override
    @Transactional
    public void setAvailability(Long timeSlotId, boolean available) {
        if (timeSlotId == null) throw new BadRequestException("timeSlotId es requerido");

        TimeSlot ts = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new NotFoundException("TimeSlot no encontrado: " + timeSlotId));

        ts.setAvailable(available);
        timeSlotRepository.save(ts);

        meterRegistry.counter("timeslot.availability.changed").increment();
        log.info("TimeSlot id={} availability={}", timeSlotId, available);
    }

    // ======================= VALIDACIONES PRIVADAS =======================

    private void validateDTO(TimeSlotDTO dto) {
        if (dto == null) throw new BadRequestException("TimeSlotDTO es requerido");
        if (dto.getFieldId() == null) throw new BadRequestException("fieldId es requerido");
        if (dto.getStartDateTime() == null) throw new BadRequestException("startDateTime es requerido");
        if (dto.getEndDateTime() == null) throw new BadRequestException("endDateTime es requerido");
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (!start.isBefore(end)) {
            throw new BadRequestException("startDateTime debe ser anterior a endDateTime");
        }
    }

    private void validateNoOverlap(Long fieldId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        List<TimeSlot> overlapping = timeSlotRepository.findOverlappingSlots(fieldId, start, end);
        boolean conflict = overlapping.stream()
                .anyMatch(ts -> excludeId == null || !ts.getId().equals(excludeId));

        if (conflict) {
            throw new ConflictException(
                    String.format("Ya existe un time slot que se superpone en el rango %s - %s", start, end)
            );
        }
    }
}
