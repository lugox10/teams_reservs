package com.lugo.teams.reservs.infrastructure.persistence.jpa;

import com.lugo.teams.reservs.domain.model.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DataOwnerRepository extends JpaRepository<Owner, Long> {
    Optional<Owner> findByEmail(String email);
}
