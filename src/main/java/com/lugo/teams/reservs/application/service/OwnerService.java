package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.owner.OwnerRequestDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerResponseDTO;

public interface OwnerService {
    OwnerResponseDTO createOwnerWithUser(OwnerRequestDTO dto);
    OwnerResponseDTO findByUserId(Long userId);
}
