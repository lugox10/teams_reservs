package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository {
    TimeSlot save(TimeSlot slot);
    Optional<TimeSlot> findById(Long id);
    List<TimeSlot> findByFieldIdAndAvailableTrue(Long fieldId);
    List<TimeSlot> findAvailableByFieldBetween(Long fieldId, LocalDateTime from, LocalDateTime to);
    List<TimeSlot> findOverlappingSlots(Long fieldId, LocalDateTime start, LocalDateTime end);
}
