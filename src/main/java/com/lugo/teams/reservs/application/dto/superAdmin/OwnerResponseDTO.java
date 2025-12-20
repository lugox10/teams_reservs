package com.lugo.teams.reservs.application.dto.superAdmin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerResponseDTO {

    private Long ownerId;
    private String businessName;

    private String username;
    private String email;

    private boolean enabled;
    private int totalVenues;
}

