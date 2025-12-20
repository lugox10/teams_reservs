 package com.lugo.teams.reservs.application.dto.owner;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para crear/editar Owner.
 * - username/email/password son para el ReservUser asociado (si procede).
 * - El service se encarga de crear/ligar el ReservUser y hashear la password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestDTO {

    @NotBlank(message = "username es obligatorio")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;      // para crear/ligar ReservUser

    @Email(message = "email inválido")
    @NotBlank(message = "email es obligatorio")
    private String email;

    @NotBlank(message = "password es obligatorio")
    @Size(min = 8, message = "password mínimo 4 caracteres")
    private String password;      // texto plano aquí — hashear en service

    @NotBlank(message = "name (nombre del negocio) es obligatorio")
    private String name;          // nombre del negocio

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "phone formato inválido")
    private String phone;

    private String address;
    private String nit;
    private String businessName;
    private String logo; // url del logo (opcional)
}
