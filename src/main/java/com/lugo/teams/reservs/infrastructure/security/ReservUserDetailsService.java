package com.lugo.teams.reservs.infrastructure.security;

import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservUserDetailsService implements UserDetailsService {

    private final ReservUserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        if (login == null) throw new UsernameNotFoundException("Login vacío");

        String normalized = login.trim();
        // normalize email to lower-case for consistent lookup
        String maybeEmail = normalized.contains("@") ? normalized.toLowerCase() : normalized;

        ReservUser user = userRepo.findByUsernameOrEmailOrIdentification(normalized, maybeEmail, normalized)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        // mapear roles a authorities (asume ReservUser.role tenga nombre compatible: e.g. ROLE_USER)
        String roleName = (user.getRole() != null) ? "ROLE_" + user.getRole().name() : "ROLE_USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername()) // no forzamos lower-case aquí: lo que importa es que DB y registro usen el mismo formato
                .password(user.getPassword())
                .authorities(roleName) // ej. "ROLE_USER" o "ROLE_OWNER"
                .accountLocked(false)
                .disabled(false)
                .build();

    }
}
