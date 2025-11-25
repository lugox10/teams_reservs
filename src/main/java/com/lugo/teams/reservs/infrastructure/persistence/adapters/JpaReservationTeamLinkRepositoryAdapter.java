package com.lugo.teams.reservs.infrastructure.persistence.adapters;

import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import com.lugo.teams.reservs.domain.repository.ReservationTeamLinkRepository;
import com.lugo.teams.reservs.infrastructure.persistence.jpa.DataReservationTeamLinkRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaReservationTeamLinkRepositoryAdapter implements ReservationTeamLinkRepository {

    private final DataReservationTeamLinkRepository repo;

    public JpaReservationTeamLinkRepositoryAdapter(DataReservationTeamLinkRepository repo) {
        this.repo = repo;
    }

    @Override
    public ReservationTeamLink save(ReservationTeamLink link) {
        return repo.save(link);
    }

    @Override
    public Optional<ReservationTeamLink> findById(Long id) {
        return repo.findById(id);
    }
}
