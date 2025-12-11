package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.owner.OwnerRequestDTO;
import com.lugo.teams.reservs.application.dto.owner.OwnerResponseDTO;
import com.lugo.teams.reservs.application.service.OwnerService;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import com.lugo.teams.reservs.shared.exception.ConflictException;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final OwnerRepository ownerRepository;
    private final ReservUserRepository reservUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OwnerResponseDTO createOwnerWithUser(OwnerRequestDTO dto) {
        if (dto == null) throw new BadRequestException("OwnerRequestDTO es requerido");

        String emailNorm = dto.getEmail().trim().toLowerCase();
        String username = dto.getUsername().trim();

        if (reservUserRepository.existsByEmail(emailNorm) || ownerRepository.existsByEmail(emailNorm)) {
            throw new ConflictException("Email ya registrado");
        }
        if (reservUserRepository.existsByUsername(username)) {
            throw new ConflictException("Username ya registrado");
        }

        // 1) crear ReservUser (auth)
        ReservUser user = new ReservUser();
        user.setUsername(username);
        user.setEmail(emailNorm);
        user.setFirstName(dto.getName());
        user.setPhone(dto.getPhone());
        user.setIdentification(null);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(ReservUserRole.OWNER);
        ReservUser savedUser = reservUserRepository.save(user);

        // 2) crear Owner (negocio) y vincular
        Owner owner = new Owner();
        owner.setName(dto.getName());
        owner.setEmail(emailNorm);
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
        // opcional: guardar password también en owner.password si querías (no recomendable duplicar)
        owner.setPassword(null);
        owner.setUser(savedUser);
        Owner savedOwner = ownerRepository.save(owner);

        return OwnerResponseDTO.builder()
                .id(savedOwner.getId())
                .name(savedOwner.getName())
                .email(savedOwner.getEmail())
                .phone(savedOwner.getPhone())
                .address(savedOwner.getAddress())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .build();
    }

    @Override
    public OwnerResponseDTO findByUserId(Long userId) {
        if (userId == null) throw new BadRequestException("userId es requerido");
        return ownerRepository.findByUserId(userId)
                .map(o -> OwnerResponseDTO.builder()
                        .id(o.getId())
                        .name(o.getName())
                        .email(o.getEmail())
                        .phone(o.getPhone())
                        .address(o.getAddress())
                        .userId(o.getUser() != null ? o.getUser().getId() : null)
                        .username(o.getUser() != null ? o.getUser().getUsername() : null)
                        .build())
                .orElse(null);
    }
}
