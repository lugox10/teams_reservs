package com.lugo.teams.reservs.application.mapper;
import com.lugo.teams.reservs.application.dto.user.ReservUserRequestDTO;
import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;
import com.lugo.teams.reservs.domain.model.ReservUser;
import org.springframework.stereotype.Component;

@Component
public class ReservUserMapper {

    /**
     * Convierte request DTO a entidad; recibe hashedPassword (hashear fuera).
     * Si req.getRole() == null -> asigna ReservUserRole.USER por defecto.
     * Si enabled/locked vienen en request, los aplica; si no, mantiene defaults de la entidad.
     */
    public ReservUser toEntity(ReservUserRequestDTO req, String hashedPassword) {
        if (req == null) return null;
        ReservUser.ReservUserBuilder builder = ReservUser.builder()
                .username(req.getUsername())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .identification(req.getIdentification())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(hashedPassword)
                .role(req.getRole() != null ? req.getRole() : com.lugo.teams.reservs.domain.model.ReservUserRole.USER);

        if (req.getEnabled() != null) builder.enabled(req.getEnabled());
        if (req.getLocked() != null) builder.locked(req.getLocked());

        return builder.build();
    }

    /**
     * Entidad -> Response DTO (no incluye password).
     */
    public ReservUserResponseDTO toResponse(ReservUser user) {
        if (user == null) return null;
        return ReservUserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .identification(user.getIdentification())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .locked(user.isLocked())
                .build();
    }
}
