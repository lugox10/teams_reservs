package com.lugo.teams.reservs.infrastructure.persistence.jpa;

import com.lugo.teams.reservs.domain.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DataFieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByVenueId(Long venueId);
    List<Field> findByActiveTrue();
}
