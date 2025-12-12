// src/main/java/com/lugo/teams/reservs/application/dto/venue/VenueRequestDTO.java
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
    @NotNull(message = "ownerId es requerido")
    private Long ownerId;

    @NotBlank(message = "name es requerido")
    private String name;

    private String address;
    private String timeZone;
    private String mainPhotoUrl;
    private Double lat;
    private Double lng;
    private Boolean active;
    private Boolean allowOnsitePayment;
    private Boolean allowBankTransfer;
    private Boolean allowOnlinePayment;
    private List<String> photos;
}
