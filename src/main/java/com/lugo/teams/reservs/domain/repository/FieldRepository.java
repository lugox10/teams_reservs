package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Field;

import java.util.List;
import java.util.Optional;

public interface FieldRepository {
    Field save(Field field);
    Optional<Field> findById(Long id);
    List<Field> findByVenueId(Long venueId);
    List<Field> findByActiveTrue();
}
