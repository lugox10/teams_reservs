package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Owner;

import java.util.Collection;
import java.util.Optional;

public interface OwnerRepository {
    Owner save(Owner owner);

    Optional<Owner> findById(Long id);

    Optional<Owner> findByEmail(String email);


    Optional<Owner> findByUserId(Long userId);

    boolean existsByEmail(String email);


    Optional<Owner> findByBusinessNameOrNameOrEmail(String login, String login1, String login2);

    Collection<Owner> findAll();
}


