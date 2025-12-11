package com.lugo.teams.reservs.application.dto.owner;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private Long userId;
    private String username;
}
