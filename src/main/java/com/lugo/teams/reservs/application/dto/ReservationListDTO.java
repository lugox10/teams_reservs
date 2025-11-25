package com.lugo.teams.reservs.application.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationListDTO {
    private Long id;
    private String userName;
    private Long fieldId;
    private String fieldName;
    private Long venueId;
    private String venueName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private ReservationStatus status;
    private BigDecimal totalAmount;
}
