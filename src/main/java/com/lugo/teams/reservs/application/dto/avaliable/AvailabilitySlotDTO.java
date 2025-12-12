
package com.lugo.teams.reservs.application.dto.avaliable;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilitySlotDTO {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String slotLabel; // ej "19:00 - 20:00"
    private AvailabilityStatus status;
    private BigDecimal price;
    private List<String> paymentOptions;
    private Long reservationId; // si está BOOKED
    private String blockedReason; // si está BLOCKED (opcional)
}
