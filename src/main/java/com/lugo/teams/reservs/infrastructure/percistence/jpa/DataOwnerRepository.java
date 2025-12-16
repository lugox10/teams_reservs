package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.model.ReservUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataOwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);

    Optional<Owner> findByUserId(Long userId);

    boolean existsByEmail(String email);

    Optional<Owner> findByBusinessNameIgnoreCaseOrNameIgnoreCaseOrEmailIgnoreCase(
            String businessName, String name, String email);

    Optional<Owner> findByUser(ReservUser user);

}
