package com.lugo.teams.reservs.application.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequestDTO {

    @NotBlank
    private String userName;

    private Long userId; // opcional

    @NotNull
    private Long fieldId;

    // Preferir timeSlotId; permitir start/end para reservas custom
    private Long timeSlotId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Min(1)
    private Integer playersCount = 1;

    private String teamName;

    private String notes;

    /**
     * Si true, se solicitará crear (opcional) match en Teams-FC tras pago confirmado.
     */
    private boolean createTeamsMatch = false;
}
