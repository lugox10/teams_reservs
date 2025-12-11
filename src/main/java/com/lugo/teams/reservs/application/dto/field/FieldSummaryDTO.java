package com.lugo.teams.reservs.application.dto.field;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldSummaryDTO {
    private Long id;
    private String name;
    private String venueName;
    private Integer capacityPlayers;
    private BigDecimal pricePerHour;
    private String firstPhoto;
}
