package com.lugo.teams.reservs.application.dto.user;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import lombok.*;

/**
 * DTO de salida para usuarios (sin password).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservUserResponseDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String identification;
    private String phone;
    private String email;
    private ReservUserRole role;
    private boolean enabled;
    private boolean locked;
}
