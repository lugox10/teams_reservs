package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.application.mapper.VenueMapper;
import com.lugo.teams.reservs.application.service.VenueService;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.Venue;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.domain.repository.VenueRepository;
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
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;
    private final OwnerRepository ownerRepository;
    private final VenueMapper venueMapper;
    private final MeterRegistry meterRegistry;

    // ================== CREATE ==================
    @Override
    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO dto) {
        if (dto == null) {
            throw new BadRequestException("VenueRequestDTO es requerido");
        }

        // aquí asumo que VenueRequestDTO tiene getOwnerId()
        if (dto.getOwnerId() == null) {
            throw new BadRequestException("ownerId es requerido");
        }

        Owner owner = ownerRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + dto.getOwnerId()));

        Venue venue = venueMapper.toEntity(dto, owner);
        Venue saved = venueRepository.save(venue);

        meterRegistry.counter("venue.created").increment();
        log.info("Venue creada id={} ownerId={} nombre={}",
                saved.getId(),
                saved.getOwner() != null ? saved.getOwner().getId() : null,
                saved.getName());

        return venueMapper.toResponseDTO(saved);
    }

    // ================== UPDATE ==================
    @Override
    @Transactional
    public VenueResponseDTO updateVenue(Long id, VenueRequestDTO dto) {
        if (id == null) {
            throw new BadRequestException("id es requerido");
        }
        if (dto == null) {
            throw new BadRequestException("VenueRequestDTO es requerido");
        }

        Venue existing = venueRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venue no encontrada: " + id));

        Owner owner = null;
        // solo buscamos owner si viene en el DTO; si no, dejamos el actual
        if (dto.getOwnerId() != null &&
                (existing.getOwner() == null || !dto.getOwnerId().equals(existing.getOwner().getId()))) {

            owner = ownerRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new NotFoundException("Owner no encontrado: " + dto.getOwnerId()));
        }

        // delegamos a mapper la actualización parcial
        venueMapper.updateEntityFromRequest(existing, dto, owner);

        Venue saved = venueRepository.save(existing);
        meterRegistry.counter("venue.updated").increment();

        log.info("Venue actualizada id={} nombre={} active={}",
                saved.getId(), saved.getName(), saved.isActive());

        return venueMapper.toResponseDTO(saved);
    }

    // ================== FIND BY ID ==================
    @Override
    public Optional<VenueResponseDTO> findById(Long id) {
        if (id == null) {
            throw new BadRequestException("id es requerido");
        }
        return venueRepository.findById(id).map(venueMapper::toResponseDTO);
    }

    // ================== FIND BY OWNER ==================
// FIND BY OWNER -> devuelve lista de resumen (VenueListDTO)
    @Override
    public List<VenueListDTO> findByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new BadRequestException("ownerId es requerido");
        }
        List<Venue> venues = venueRepository.findByOwnerId(ownerId);
        return venueMapper.toResponseDTOList(venues); // List<VenueListDTO>
    }

    // FIND ACTIVE -> también debe devolver lista de resumen (VenueListDTO)
    @Override
    public List<VenueResponseDTO> findActive() {
        List<Venue> venues = venueRepository.findByActiveTrue();
        return venues.stream().map(venueMapper::toResponseDTO).collect(Collectors.toList()); // map a detalle por item
    }



}
