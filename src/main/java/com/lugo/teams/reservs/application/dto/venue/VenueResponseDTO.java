package com.lugo.teams.reservs.application.dto.venue;

import com.lugo.teams.reservs.application.dto.owner.OwnerSummaryDTO;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponseDTO {
    private Long id;
    private OwnerSummaryDTO owner;
    private String nombre;
    private String direccion;
    private String tipoDeporte;
    private boolean active;
    private List<String> photos;
    private String timeZone;
}
