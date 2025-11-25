package com.lugo.teams.reservs.integration.teamsfc.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamsMatchDTO {
    private Long id;
    private String url;
    private String status;
}
