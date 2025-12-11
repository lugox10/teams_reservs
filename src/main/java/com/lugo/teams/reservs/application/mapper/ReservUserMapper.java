package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.user.ReservUserRequestDTO;
import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;
import com.lugo.teams.reservs.domain.model.ReservUser;
import org.springframework.stereotype.Component;

@Component
public class ReservUserMapper {

    public ReservUser toEntity(ReservUserRequestDTO req, String hashedPassword) {
        if (req == null) return null;
        return ReservUser.builder()
                .username(req.getUsername())
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .identification(req.getIdentification())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(hashedPassword)
                .role(com.lugo.teams.reservs.domain.model.ReservUserRole.USER)
                .build();
    }

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
                .role(user.getRole()) // ya es ReservUserRole en la entidad
                .build();
    }
}
