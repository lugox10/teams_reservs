package com.lugo.teams.reservs.application.dto.reserv;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationTeamLinkDTO {
    private Long id;
    private Long teamsFcMatchId;
    private String teamsFcUrl;
    private String teamName;
    private LocalDateTime createdAt;
}
