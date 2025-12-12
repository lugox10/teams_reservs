package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.mapper.FieldMapper;
import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.model.Venue;
import com.lugo.teams.reservs.domain.repository.FieldRepository;
import com.lugo.teams.reservs.domain.repository.VenueRepository;
import com.lugo.teams.reservs.application.service.FieldService;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FieldServiceImpl implements FieldService {

    private static final Logger log = LoggerFactory.getLogger(FieldServiceImpl.class);

    private final FieldRepository fieldRepository;
    private final VenueRepository venueRepository;
    private final FieldMapper mapper;
    private final MeterRegistry meterRegistry;

    // CREATE
    @Override
    @Transactional
    public FieldDTO createField(FieldDTO dto) {
        if (dto == null) throw new BadRequestException("FieldDTO es requerido");
        if (dto.getVenueId() == null) throw new BadRequestException("venueId es requerido");
        if (dto.getName() == null || dto.getName().trim().isEmpty())
            throw new BadRequestException("name es requerido");

        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new NotFoundException("Venue no encontrado: " + dto.getVenueId()));

        // si quisieras validar unicidad: existsByVenueIdAndName(...)


        Field entity = new Field();
        Field saved = fieldRepository.save(entity);
        meterRegistry.counter("field.created").increment();
        log.info("Field creado id={} venueId={} name={}", saved.getId(), venue.getId(), saved.getName());
        return mapper.toDTO(saved);
    }

    // UPDATE
    @Override
    @Transactional
    public FieldDTO updateField(Long id, FieldDTO dto) {
        if (id == null) throw new BadRequestException("id es requerido");
        if (dto == null) throw new BadRequestException("FieldDTO es requerido");

        Field existing = fieldRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Field no encontrado: " + id));

        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getFieldType() != null) existing.setFieldType(dto.getFieldType());
        if (dto.getSurface() != null) existing.setSurface(dto.getSurface());
        if (dto.getCapacityPlayers() != null) {
            if (dto.getCapacityPlayers() < 1)
                throw new BadRequestException("capacityPlayers debe ser >= 1");
            existing.setCapacityPlayers(dto.getCapacityPlayers());
        }
        if (dto.getPricePerHour() != null) existing.setPricePerHour(dto.getPricePerHour());
        if (dto.getPhotos() != null) existing.setPhotos(dto.getPhotos());
        if (dto.getActive() != null) existing.setActive(dto.getActive());

        if (dto.getVenueId() != null &&
                !dto.getVenueId().equals(existing.getVenue() != null ? existing.getVenue().getId() : null)) {

            Venue v = venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new NotFoundException("Venue no encontrado: " + dto.getVenueId()));
            existing.setVenue(v);
        }

        Field saved = fieldRepository.save(existing);
        meterRegistry.counter("field.updated").increment();
        log.info("Field actualizado id={} name={} active={}", saved.getId(), saved.getName(), saved.isActive());
        return mapper.toDTO(saved);
    }

    @Override
    public Optional<FieldDTO> findById(Long id) {
        if (id == null) throw new BadRequestException("id es requerido");
        return fieldRepository.findById(id).map(mapper::toDTO);
    }

    @Override
    public List<FieldDTO> findByVenueId(Long venueId) {
        if (venueId == null) throw new BadRequestException("venueId es requerido");
        var list = fieldRepository.findByVenueId(venueId);
        return list == null ? List.of() : list.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<FieldDTO> findActiveFields() {
        var list = fieldRepository.findByActiveTrue();
        return list == null ? List.of() : list.stream().map(mapper::toDTO).collect(Collectors.toList());
    }

@Override
@Transactional(readOnly = true)
public Optional<FieldDetailDTO> findDetailById(Long id) {
    if (id == null) throw new BadRequestException("id es requerido");
    return fieldRepository.findWithDetailsById(id).map(mapper::toDetailDTO);
}


}
