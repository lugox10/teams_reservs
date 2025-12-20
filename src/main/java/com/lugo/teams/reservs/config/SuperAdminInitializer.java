package com.lugo.teams.reservs.config;

import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer {

    private final ReservUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        String superUser = "superadmin";
        if (userRepo.findByUsername(superUser).isEmpty()) {
            ReservUser s = ReservUser.builder()
                    .username(superUser)
                    .email("superadmin@lugofutbol.com")
                    .password(passwordEncoder.encode("TEST123"))
                    .role(ReservUserRole.SUPERADMIN)
                    .enabled(true)
                    .locked(false)
                    .build();
            userRepo.save(s);
        }
    }
}