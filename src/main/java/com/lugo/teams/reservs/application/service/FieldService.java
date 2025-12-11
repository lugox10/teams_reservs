package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones sobre campos (cancha).
 */
public interface FieldService {

    FieldDTO createField(FieldDTO dto);

    FieldDTO updateField(Long id, FieldDTO dto);

    Optional<FieldDTO> findById(Long id);

    List<FieldDTO> findByVenueId(Long venueId);

    List<FieldDTO> findActiveFields();
}
