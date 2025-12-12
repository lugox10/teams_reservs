package com.lugo.teams.reservs.application.dto.user;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para creación/edición de reserv users.
 * - username: obligatorio, 3..50 chars
 * - email: obligatorio y válido
 * - password: obligatorio, min 8 chars
 * - phone: opcional, patrón simple (ajusta regex si necesitas otro formato)
 * - role: opcional; si no viene el mapper asigna USER
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservUserRequestDTO {

    @NotBlank(message = "username es obligatorio")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;

    private String firstName;
    private String lastName;
    private String identification;

    @Pattern(regexp = "^[0-9+()\\-\\s]{7,25}$", message = "phone formato inválido")
    private String phone;

    @Email(message = "email inválido")
    @NotBlank(message = "email es obligatorio")
    private String email;

    @NotBlank(message = "password es obligatorio")
    @Size(min = 8, message = "password mínimo 8 caracteres")
    private String password;

    /**
     * Si el request viene con role, el mapper lo respetará. Si es null, por defecto USER.
     * Usamos el enum ReservUserRole para tipado fuerte.
     */
    private com.lugo.teams.reservs.domain.model.ReservUserRole role;

    /**
     * Flags administrativos (opcionales en request; solo admins deberían poder cambiarlos).
     */
    private Boolean enabled;
    private Boolean locked;
}
