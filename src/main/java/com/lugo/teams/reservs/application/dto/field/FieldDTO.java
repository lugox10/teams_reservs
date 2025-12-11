package com.lugo.teams.reservs.application.dto.field;


import com.lugo.teams.reservs.domain.model.FieldType;
import com.lugo.teams.reservs.domain.model.SurfaceType;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldDTO {
    private Long id;
    private Long venueId;
    private String name;
    private FieldType fieldType;
    private SurfaceType surface;
    private Integer capacityPlayers;
    private BigDecimal pricePerHour;
    private Boolean active;
    private List<String> photos;
}
