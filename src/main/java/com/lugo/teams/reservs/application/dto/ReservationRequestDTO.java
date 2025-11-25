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

    // O bien timeSlotId o start/end explícitos
    private Long timeSlotId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Min(1)
    private Integer playersCount = 1;

    private String teamName;

    private String notes;

    /**
     * Si true, intentaremos (opcional) crear un match en Teams-FC después de pago confirmado.
     * Este comportamiento depende de integración (Feign client) y permisos del owner.
     */
    private boolean createTeamsMatch = false;
}
