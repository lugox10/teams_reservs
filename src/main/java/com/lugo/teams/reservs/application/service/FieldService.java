package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.field.FieldDTO;
import com.lugo.teams.reservs.application.dto.field.FieldDetailDTO;
import com.lugo.teams.reservs.application.dto.field.FieldRequestDTO;
import com.lugo.teams.reservs.application.dto.field.FieldSummaryDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones sobre campos (cancha).
 */
public interface FieldService {

    FieldDTO createField(Long venueId, FieldRequestDTO dto);

    FieldDTO updateField(Long id, FieldDTO dto);

    Optional<FieldDTO> findById(Long id);

    List<FieldDetailDTO> findByVenueId(Long venueId);

    List<FieldDTO> findActiveFields();



    Optional<FieldDetailDTO> findDetailById(Long id);



    List<FieldSummaryDTO> findSummariesByVenueId(Long id);

    List<LocalTime> getBookedHoursForDate(Long id, LocalDate date);
}
