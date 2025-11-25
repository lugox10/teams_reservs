package com.lugo.teams.reservs.infrastructure.persistence.adapters;

import com.lugo.teams.reservs.domain.model.Owner;
import com.lugo.teams.reservs.domain.repository.OwnerRepository;
import com.lugo.teams.reservs.infrastructure.persistence.jpa.DataOwnerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaOwnerRepositoryAdapter implements OwnerRepository {

    private final DataOwnerRepository repo;

    public JpaOwnerRepositoryAdapter(DataOwnerRepository repo) {
        this.repo = repo;
    }

    @Override
    public Owner save(Owner owner) {
        return repo.save(owner);
    }

    @Override
    public Optional<Owner> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public Optional<Owner> findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
