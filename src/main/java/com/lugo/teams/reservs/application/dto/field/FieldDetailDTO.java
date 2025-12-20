
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
public class FieldDetailDTO {
    private Long id;
    private Long venueId;
    private String venueName;

    private String name;
    private FieldType fieldType;
    private SurfaceType surface;
    private Integer capacityPlayers;
    private BigDecimal pricePerHour;

    private Integer slotMinutes;
    private Integer openHour;
    private Integer closeHour;
    private Integer minBookingHours;
    private BigDecimal lat;
    private BigDecimal lng;

    private List<String> photos;

    /**
     * Payment options available for this field (derived from venue flags).
     * Ej: ["ONSITE","BANK","ONLINE"]
     */
    private List<String> paymentOptions;
}
