package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.venue.VenueListDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueRequestDTO;
import com.lugo.teams.reservs.application.dto.venue.VenueResponseDTO;
import com.lugo.teams.reservs.domain.model.Venue;

import java.util.List;
import java.util.Optional;

/**
 * Gestión de sedes / complejos deportivos.
 */
public interface VenueService {

    VenueResponseDTO createVenue(VenueRequestDTO dto);

    VenueResponseDTO updateVenue(Long id, VenueRequestDTO dto);

    Optional<VenueResponseDTO> findById(Long id);

    List<VenueListDTO> findByOwnerId(Long ownerId);

    List<VenueResponseDTO> findActive();

    List<Venue> findEntitiesByOwnerId(Long ownerId);
}

