package com.lugo.teams.reservs.application.dto.reserv;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.Duration;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO con validaciones adicionales:
 * - bloques de hora (minutos y segundos = 0)
 * - durationMinutes múltiplo de 60 y entre 60 y 120 (max 2h por reserva)
 * - cross-field checks (timeSlotId OR start+end)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {

    private String userName;
    private Long userId;

    private Long venueId;

    @NotNull(message = "fieldId es obligatorio")
    private Long fieldId;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    /**
     * Duración en minutos de la reserva solicitada.
     * Restricción: 60 (1h) hasta 120 (2h) por reserva.
     * Si no proporcionas durationMinutes, se inferirá de start/end.
     */
    @Min(value = 60, message = "durationMinutes mínimo 60")
    @Max(value = 120, message = "durationMinutes máximo 120")
    private Integer durationMinutes = 60;

    @Min(value = 1, message = "playersCount mínimo 1")
    private Integer playersCount = 1;

    private String teamName;
    private String notes;

    private boolean createTeamsMatch = false;

    private String guestName;
    private String guestPhone;
    private String guestEmail;


}
