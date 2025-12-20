package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Venue;

import java.util.List;
import java.util.Optional;

public interface VenueRepository {
    Venue save(Venue venue);
    Optional<Venue> findById(Long id);
    List<Venue> findByOwnerId(Long ownerId);
    List<Venue> findByActiveTrue();

    Venue saveAndFlush(Venue venue);

    int countByOwnerId(Long id);
}
