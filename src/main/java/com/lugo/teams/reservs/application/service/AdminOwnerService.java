package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.superAdmin.OwnerCreateRequestDTO;
import com.lugo.teams.reservs.application.dto.superAdmin.OwnerResponseDTO;

import java.util.List;

public interface AdminOwnerService {

    OwnerResponseDTO createOwner(OwnerCreateRequestDTO dto);

    List<OwnerResponseDTO> findAllOwners();

    OwnerResponseDTO findById(Long ownerId);

    void enableOwner(Long ownerId);

    void disableOwner(Long ownerId);
}

