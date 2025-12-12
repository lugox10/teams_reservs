package com.lugo.teams.reservs.application.dto.slot;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para TimeSlot. startDateTime y endDateTime obligatorios al crear/actualizar slot.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TimeSlotDTO {
    private Long id;

    @NotNull(message = "fieldId es obligatorio")
    private Long fieldId;

    @NotNull(message = "startDateTime es obligatorio")
    private LocalDateTime startDateTime;

    @NotNull(message = "endDateTime es obligatorio")
    private LocalDateTime endDateTime;

    @DecimalMin(value = "0.0", inclusive = true, message = "priceOverride no puede ser negativo")
    private BigDecimal priceOverride;

    private Boolean available; // null = no especificado (mantener default de la entidad)




}
