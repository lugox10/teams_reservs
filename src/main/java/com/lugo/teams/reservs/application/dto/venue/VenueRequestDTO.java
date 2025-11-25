package com.lugo.teams.reservs.application.dto.venue;

import lombok.Data;

@Data
public class VenueRequestDTO {
    private Long ownerId;
    private String nombre;
    private String direccion;
    private String tipoDeporte; // Futbol, Baloncesto, Tenis...
}
