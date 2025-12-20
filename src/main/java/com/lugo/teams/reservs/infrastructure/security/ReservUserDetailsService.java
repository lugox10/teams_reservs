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
        if (login == null || login.isBlank()) {
            throw new UsernameNotFoundException("Login vacío");
        }

        String value = login.trim();
        String email = value.contains("@") ? value.toLowerCase() : value;

        ReservUser user = userRepo
                .findByUsernameOrEmailOrIdentification(value, email, value)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        return new UserDetailsImpl(user); // <-- aquí usamos tu UserDetailsImpl
    }

}
