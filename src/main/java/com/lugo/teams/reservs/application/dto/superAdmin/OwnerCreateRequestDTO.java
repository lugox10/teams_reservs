package com.lugo.teams.reservs.application.dto.superAdmin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerCreateRequestDTO {

    @NotBlank private String username;

    @Email @NotBlank private String email;

    @NotBlank @Size(min = 8) private String password;

    @NotBlank private String name;           // persona de contacto

    private String businessName;
    private String phone;
    private String address;
    private String nit;
    private String logo;
}
