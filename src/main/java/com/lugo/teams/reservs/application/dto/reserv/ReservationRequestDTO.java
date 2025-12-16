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

    private Long timeSlotId;

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

    @AssertTrue(message = "Debe proporcionar timeSlotId o startDateTime y endDateTime")
    private boolean isTimeSpecificationValid() {
        if (this.timeSlotId != null) return true;
        return this.startDateTime != null && this.endDateTime != null;
    }

    @AssertTrue(message = "startDateTime debe ser anterior a endDateTime")
    private boolean isStartBeforeEnd() {
        if (this.startDateTime == null || this.endDateTime == null) return true;
        return this.startDateTime.isBefore(this.endDateTime);
    }

    @AssertTrue(message = "startDateTime y endDateTime deben estar en inicio de hora (minutos=0, segundos=0)")
    private boolean isOnHourBoundary() {
        if (this.startDateTime == null || this.endDateTime == null) return true;
        return this.startDateTime.getMinute() == 0 && this.startDateTime.getSecond() == 0
                && this.endDateTime.getMinute() == 0 && this.endDateTime.getSecond() == 0;
    }

    @AssertTrue(message = "La duración debe coincidir con endDateTime - startDateTime")
    private boolean isDurationConsistent() {
        if (this.startDateTime == null || this.endDateTime == null) return true;
        long diffMin = Duration.between(this.startDateTime, this.endDateTime).toMinutes();
        return this.durationMinutes != null && diffMin == this.durationMinutes;
    }

    @AssertTrue(message = "durationMinutes debe ser múltiplo de 60 (bloques de 1 hora)")
    private boolean isDurationWithinHourBlocks() {
        if (this.durationMinutes == null) return true;
        return this.durationMinutes % 60 == 0;
    }
}
