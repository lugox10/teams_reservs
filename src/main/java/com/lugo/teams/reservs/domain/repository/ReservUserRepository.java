package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.ReservUser;

import java.util.Optional;

public interface ReservUserRepository {

    ReservUser save(ReservUser user);

    Optional<ReservUser> findById(Long id);

    Optional<ReservUser> findByEmail(String email);

    Optional<ReservUser> findByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<ReservUser> findByUsername(String username);

    Optional<ReservUser> findByUsernameOrEmailOrIdentification(
            String username,
            String email,
            String identification
    );

    Optional<ReservUser> findByIdentification(String identification);

    boolean existsByUsername(String username);

    boolean existsByIdentification(String identification);
}
