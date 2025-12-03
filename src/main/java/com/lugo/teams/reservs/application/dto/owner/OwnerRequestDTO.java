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
    private String nombre;

    private String telefono;

    @Email
    private String email;

    private String address;
}
