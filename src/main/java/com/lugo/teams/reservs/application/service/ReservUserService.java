package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.user.ReservUserRequestDTO;
import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;

import java.util.Optional;

public interface ReservUserService {
    ReservUserResponseDTO register(ReservUserRequestDTO dto);
    Optional<ReservUserResponseDTO> findByEmail(String email);
    Optional<ReservUserResponseDTO> findById(Long id);
    boolean existsByEmail(String email);

    Optional<ReservUserResponseDTO> findByUsername(String username);
}
