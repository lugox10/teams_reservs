package com.lugo.teams.reservs.application.dto.owner;
import lombok.*;

/**
 * DTO ligero para listados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerSummaryDTO {
    private Long id;
    private String name;
}
