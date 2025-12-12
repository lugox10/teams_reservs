
package com.lugo.teams.reservs.application.service;



import com.lugo.teams.reservs.application.dto.avaliable.AvailabilityResponseDTO;

import java.time.LocalDate;

public interface FieldAvailabilityService {
    AvailabilityResponseDTO getAvailability(Long fieldId, LocalDate date);
}
