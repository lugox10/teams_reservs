package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.domain.model.Field;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    // en DataReservationRepository (recomendado)
    @Query("""
  select r.startDateTime
  from Reservation r
  where r.field = :field
    and r.startDateTime >= :start
    and r.startDateTime < :end
    and r.status = 'CONFIRMED'
""")
    List<LocalDateTime> findBookedStartDateTimesBetween(
            @Param("field") Field field,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );








}
