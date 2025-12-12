package com.lugo.teams.reservs.application.dto.reserv;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.AssertTrue;

/**
 * Reglas:
 * - fieldId es obligatorio.
 * - Debe proporcionarse EITHER timeSlotId OR (startDateTime y endDateTime).
 * - durationMinutes entre 1 y 60 si se envía.
 * - playersCount mínimo 1.
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

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    // nueva duración (minutos)
    @Min(value = 1, message = "durationMinutes mínimo 1")
    @Max(value = 60, message = "durationMinutes máximo 60")
    private Integer durationMinutes;

    @Min(value = 1, message = "playersCount mínimo 1")
    private Integer playersCount = 1;

    private String teamName;
    private String notes;

    private boolean createTeamsMatch = false;

    // guest fields (si reserva como invitado)
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    /**
     * Validación cross-field: se exige que el request traiga EITHER timeSlotId OR start+end.
     * Esto evita reglas complejas en el controller; si quieres externalizar a un Validator
     * personalizado lo hacemos sin drama.
     */
    @AssertTrue(message = "Debe proporcionar timeSlotId o startDateTime y endDateTime")
    private boolean isTimeSpecificationValid() {
        if (this.timeSlotId != null) return true;
        return this.startDateTime != null && this.endDateTime != null;
    }
}
