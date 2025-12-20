package com.lugo.teams.reservs.application.dto.field;
import com.lugo.teams.reservs.domain.model.FieldType;
import com.lugo.teams.reservs.domain.model.SurfaceType;
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
    private FieldType fieldType;
    private SurfaceType surface;
    private Integer slotMinutes;



}
