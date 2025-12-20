package com.lugo.teams.reservs.config;

import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;



@Profile("dev")
@Component
@RequiredArgsConstructor
@Slf4j
public class DevOwnerInitializer implements ApplicationRunner {

    private final ReservUserRepository userRepo;
    private final OwnerRepository ownerRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = "owner.upb@lugo.com";

        if (ownerRepo.findByEmail(email).isPresent()) {
            log.info("DEV INIT: Owner already exists -> {}", email);
            return;
        }

        ReservUser user = userRepo.findByEmail(email)
                .orElseGet(() -> userRepo.save(
                        ReservUser.builder()
                                .username("upb")
                                .email(email)
                                .password(passwordEncoder.encode("test"))
                                .role(ReservUserRole.OWNER)
                                .enabled(true)
                                .locked(false)
                                .build()
                ));

        Owner owner = Owner.builder()
                .name("sports-upb")
                .businessName("Deportes UPB S.A.S.")
                .email(email)
                .phone("+571300000001")
                .address("Calle 100 #10-10, Bogotá")
                .user(user)
                .build();

        ownerRepo.save(owner);
        log.info("DEV INIT: Owner creado correctamente");
    }
}
