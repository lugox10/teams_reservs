package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.TimeSlotDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona franjas horarias (time slots).
 */
public interface TimeSlotService {

    TimeSlotDTO createTimeSlot(TimeSlotDTO dto);

    TimeSlotDTO updateTimeSlot(Long id, TimeSlotDTO dto);

    Optional<TimeSlotDTO> findById(Long id);

    List<TimeSlotDTO> findAvailableByFieldBetween(Long fieldId, LocalDateTime from, LocalDateTime to);

    List<TimeSlotDTO> findByFieldId(Long fieldId);

    void setAvailability(Long timeSlotId, boolean available);
}
