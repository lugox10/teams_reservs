package com.lugo.teams.reservs.application.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueDTO {
    private Long id;
    private String name;
    private String address;
    private Long ownerId;
    private boolean active;
    private List<String> photos; // rutas/urls relativas a /uploads o CDN
    private String timeZone;
}
