package com.lugo.teams.reservs.application.dto.loginRest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {
    private String username;
    private String role;
    private String token; // JWT
}
