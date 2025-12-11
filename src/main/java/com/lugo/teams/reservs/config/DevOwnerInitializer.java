package com.lugo.teams.reservs.config;

import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import com.lugo.teams.reservs.domain.model.ReservUserRole;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.domain.repository.ReservUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DevOwnerInitializer implements ApplicationRunner {

    private final ReservUserRepository reservUserRepository;
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        createDevOwnerIfMissing();
    }

    private void createDevOwnerIfMissing() {
        final String ownerEmail = "owner.lugo@example.com";
        final String ownerUsername = "owner_lugo";
        final String rawPassword = "password123";

        // 1) Crear ReservUser (auth) si no existe
        ReservUser user = reservUserRepository.findByEmail(ownerEmail)
                .orElseGet(() -> {
                    ReservUser u = new ReservUser();
                    u.setUsername(ownerUsername);
                    u.setEmail(ownerEmail);
                    u.setFirstName("Lugo");
                    u.setLastName("Propietario");
                    u.setIdentification("OWN-1001");
                    u.setPhone("+571300000001");
                    u.setPassword(passwordEncoder.encode(rawPassword));
                    u.setRole(ReservUserRole.OWNER);
                    return reservUserRepository.save(u);
                });

        // 2) Crear Owner (business) si no existe y vincularlo al ReservUser
        Optional<Owner> existingOwner = ownerRepository.findByEmail(ownerEmail);
        if (existingOwner.isPresent()) {
            System.out.println("DEV INIT: Owner already exists -> " + ownerEmail);
            // Aseguramos vínculo si Owner existe pero no tiene user enlazado
            Owner o = existingOwner.get();
            if (o.getUser() == null) {
                o.setUser(user);
                // No sobreescribimos owner.password (si lo tienes) - recomendamos mantener auth en ReservUser
                ownerRepository.save(o);
                System.out.println("DEV INIT: linked existing Owner to ReservUser -> " + ownerEmail);
            }
            return;
        }

        Owner owner = Owner.builder()
                .name("Lugo Sports Management")
                .email(ownerEmail)
                .phone("+571300000001")
                .address("Calle 100 #10-10, Bogotá")
                // dejamos owner.password null para no duplicar credenciales (auth se maneja en ReservUser)
                .password(null)
                .user(user) // requiere que Owner tenga campo 'private ReservUser user;'
                .build();

        ownerRepository.save(owner);
        System.out.println("DEV INIT: created Owner + ReservUser -> " + ownerEmail + " / " + ownerUsername);
    }
}
