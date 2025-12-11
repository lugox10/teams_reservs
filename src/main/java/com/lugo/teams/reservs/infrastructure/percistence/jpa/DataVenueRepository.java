    package com.lugo.teams.reservs.infrastructure.percistence.jpa;

    import com.lugo.teams.reservs.domain.model.Venue;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.List;

    public interface DataVenueRepository extends JpaRepository<Venue, Long> {
        List<Venue> findByOwnerId(Long ownerId);
        List<Venue> findByActiveTrue();
    }
