package com.lugo.teams.reservs.application.dto.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestDTO {
    @NotBlank
    private String username;      // para ReservUser.username
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String password;      // texto plano aquí — lo hasheará el service
    private String name;          // nombre del negocio
    private String phone;
    private String address;
}
