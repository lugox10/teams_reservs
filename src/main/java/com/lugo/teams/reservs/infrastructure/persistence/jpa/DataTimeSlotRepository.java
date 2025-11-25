package com.lugo.teams.reservs.infrastructure.persistence.jpa;

import com.lugo.teams.reservs.domain.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DataTimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByFieldIdAndAvailableTrue(Long fieldId);

    @Query("select ts from TimeSlot ts " +
            "where ts.field.id = :fieldId " +
            "and ts.available = true " +
            "and ts.startDateTime >= :from " +
            "and ts.endDateTime <= :to " +
            "order by ts.startDateTime")
    List<TimeSlot> findAvailableByFieldBetween(@Param("fieldId") Long fieldId,
                                               @Param("from") LocalDateTime from,
                                               @Param("to") LocalDateTime to);

    @Query("select ts from TimeSlot ts " +
            "where ts.field.id = :fieldId " +
            "and not (ts.endDateTime <= :start or ts.startDateTime >= :end)")
    List<TimeSlot> findOverlappingSlots(@Param("fieldId") Long fieldId,
                                        @Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
}
