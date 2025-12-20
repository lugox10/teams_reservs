package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.superAdmin.OwnerCreateRequestDTO;
import com.lugo.teams.reservs.application.dto.superAdmin.OwnerResponseDTO;
import com.lugo.teams.reservs.application.service.AdminOwnerService;
import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import com.lugo.teams.reservs.domain.repository.VenueRepository;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminOwnerServiceImpl implements AdminOwnerService {

    private final OwnerRepository ownerRepository;
    private final ReservUserRepository reservUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final VenueRepository venueRepository;

    @Override
    public OwnerResponseDTO createOwner(OwnerCreateRequestDTO dto) {
        // 1) duplicados: username / email
        boolean exists = reservUserRepository.findByUsernameOrEmailOrIdentification(
                dto.getUsername(), dto.getEmail(), dto.getUsername()
        ).isPresent();

        if (exists) {
            throw new BadRequestException("Username o email ya existen");
        }

        // 2) crear ReservUser
        ReservUser user = ReservUser.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(ReservUserRole.OWNER)
                .enabled(true)
                .locked(false)
                .build();

        ReservUser savedUser = reservUserRepository.save(user);

        // 3) crear Owner vinculado
        Owner owner = Owner.builder()
                .name(dto.getName())
                .businessName(dto.getBusinessName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .nit(dto.getNit())
                .logo(dto.getLogo())
                .user(savedUser)
                .build();

        Owner savedOwner = ownerRepository.save(owner);
        log.info("Owner creado id={} business={}", savedOwner.getId(), savedOwner.getBusinessName());

        return mapToResponse(savedOwner, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerResponseDTO> findAllOwners() {
        return ownerRepository.findAll().stream()
                .map(owner -> {
                    int venues = (int) venueRepository.countByOwnerId(owner.getId());
                    return mapToResponse(owner, venues);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OwnerResponseDTO findById(Long ownerId) {
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado"));
        int venues = (int) venueRepository.countByOwnerId(ownerId);
        return mapToResponse(owner, venues);
    }

    @Override
    public void enableOwner(Long ownerId) {
        Owner owner = getOwner(ownerId);
        ReservUser user = owner.getUser();
        if (user == null) throw new NotFoundException("ReservUser no vinculado al Owner");
        user.setEnabled(true);
        reservUserRepository.save(user);
    }

    @Override
    public void disableOwner(Long ownerId) {
        Owner owner = getOwner(ownerId);
        ReservUser user = owner.getUser();
        if (user == null) throw new NotFoundException("ReservUser no vinculado al Owner");
        user.setEnabled(false);
        reservUserRepository.save(user);
    }

    // helpers
    private Owner getOwner(Long id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Owner no encontrado"));
    }

    private OwnerResponseDTO mapToResponse(Owner owner, int venues) {
        ReservUser user = owner.getUser();
        return OwnerResponseDTO.builder()
                .ownerId(owner.getId())
                .businessName(owner.getBusinessName())
                .username(user != null ? user.getUsername() : null)
                .email(user != null ? user.getEmail() : null)
                .enabled(user != null && user.isEnabled())
                .totalVenues(venues)
                .build();
    }
}
