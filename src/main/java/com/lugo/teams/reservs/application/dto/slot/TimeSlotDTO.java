package com.lugo.teams.reservs.application.dto.slot;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDTO {
    private Long id;
    private Long fieldId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal priceOverride;
    private boolean available;
}
