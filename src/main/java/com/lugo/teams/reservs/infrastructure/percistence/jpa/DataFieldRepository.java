package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.domain.model.Field;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataFieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByVenueId(Long venueId);
    List<Field> findByActiveTrue();

    /**
     * Trae Field con relaciones necesarias (venue, photos, timeSlots) para mapear a FieldDetailDTO
     * en el service sin LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"venue", "photos", "timeSlots"})
    Optional<Field> findWithDetailsById(Long id);

    Optional<FieldDetailDTO> findDetailById(Long id);
}
