package com.lugo.teams.reservs.application.dto.venue;

import lombok.Data;

@Data
public class VenueResponseDTO {
    private Long id;
    private OwnerSummaryDTO owner;
    private String nombre;
    private String direccion;
    private String tipoDeporte;
}
