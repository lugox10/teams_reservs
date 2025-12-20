package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.domain.model.ReservUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataReservUserRepository extends JpaRepository<ReservUser, Long> {
    Optional<ReservUser> findByUsername(String username);
    Optional<ReservUser> findByEmail(String email);
    Optional<ReservUser> findByIdentification(String identification);


    Optional<ReservUser> findByUsernameOrEmailOrIdentification(String username, String email, String identification);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByIdentification(String identification);

    Optional<ReservUser> findByPhone(String phone);


    boolean existsByUsernameOrEmail(String username, String email);
}
