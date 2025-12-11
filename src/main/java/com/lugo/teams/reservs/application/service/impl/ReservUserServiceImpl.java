package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.user.ReservUserRequestDTO;
import com.lugo.teams.reservs.application.dto.user.ReservUserResponseDTO;
import com.lugo.teams.reservs.application.mapper.ReservUserMapper;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import com.lugo.teams.reservs.application.service.ReservUserService;
import com.lugo.teams.reservs.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservUserServiceImpl implements ReservUserService {

    private final ReservUserRepository userRepo;
    private final ReservUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ReservUserResponseDTO register(ReservUserRequestDTO dto) {
        if (dto == null) throw new IllegalArgumentException("ReservUserRequestDTO es requerido");

        if (dto.getEmail() == null || dto.getEmail().isBlank()) throw new IllegalArgumentException("email es requerido");
        if (dto.getUsername() == null || dto.getUsername().isBlank()) throw new IllegalArgumentException("username es requerido");
        if (dto.getPassword() == null || dto.getPassword().isBlank()) throw new IllegalArgumentException("password es requerido");

        String emailNorm = dto.getEmail().trim().toLowerCase();
        String usernameNorm = dto.getUsername().trim().toLowerCase(); // normalizamos username a minúsculas


        if (userRepo.existsByEmail(emailNorm)) {
            throw new ConflictException("Email ya registrado");
        }
        if (userRepo.existsByUsername(usernameNorm)) {
            throw new ConflictException("Username ya registrado");
        }
        if (dto.getIdentification() != null && userRepo.existsByIdentification(dto.getIdentification().trim())) {
            throw new ConflictException("Identificación ya registrada");
        }

        String hashed = passwordEncoder.encode(dto.getPassword());
        ReservUser entity = mapper.toEntity(dto, hashed);
        entity.setEmail(emailNorm);
        entity.setUsername(usernameNorm);

        ReservUser saved = userRepo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public Optional<ReservUserResponseDTO> findByEmail(String email) {
        return userRepo.findByEmail(email).map(mapper::toResponse);
    }

    @Override
    public Optional<ReservUserResponseDTO> findById(Long id) {
        return userRepo.findById(id).map(mapper::toResponse);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    @Override
    public Optional<ReservUserResponseDTO> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepo.findByUsername(username).map(mapper::toResponse);
    }

}
