package com.lugo.teams.reservs.application.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservUserRequestDTO {
    @NotBlank
    private String username;
    private String firstName;
    private String lastName;
    private String identification;

    private String phone;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
    private String role;
}
