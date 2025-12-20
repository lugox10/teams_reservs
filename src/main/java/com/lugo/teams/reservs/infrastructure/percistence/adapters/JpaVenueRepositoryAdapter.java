package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.Venue;
import com.lugo.teams.reservs.domain.repository.VenueRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataVenueRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaVenueRepositoryAdapter implements VenueRepository {

    private final DataVenueRepository repo;

    public JpaVenueRepositoryAdapter(DataVenueRepository repo) {
        this.repo = repo;
    }

    @Override
    public Venue save(Venue venue) {
        return repo.save(venue);
    }

    @Override
    public Optional<Venue> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Venue> findByOwnerId(Long ownerId) {
        return repo.findByOwnerId(ownerId);
    }

    @Override
    public List<Venue> findByActiveTrue() {
        return repo.findByActiveTrue();
    }

    @Override
    public Venue saveAndFlush(Venue venue) {
        return repo.saveAndFlush(venue);
    }

    @Override
    public int countByOwnerId(Long id) {
        return repo.countByOwnerId(id);
    }
}
