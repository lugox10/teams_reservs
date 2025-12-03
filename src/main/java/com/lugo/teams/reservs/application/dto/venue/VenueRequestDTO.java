package com.lugo.teams.reservs.application.dto.venue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueRequestDTO {
    @NotNull
    private Long ownerId;

    @NotBlank
    private String nombre;

    private String direccion;

    private String tipoDeporte; // p.ej. "Futbol"
    private String timeZone;

    public List<String> photos;

}
