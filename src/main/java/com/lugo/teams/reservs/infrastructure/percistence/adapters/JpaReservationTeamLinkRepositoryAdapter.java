package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import com.lugo.teams.reservs.domain.repository.ReservationTeamLinkRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataReservationTeamLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaReservationTeamLinkRepositoryAdapter implements ReservationTeamLinkRepository {

    private final DataReservationTeamLinkRepository repo;

    @Override
    @Transactional
    public ReservationTeamLink save(ReservationTeamLink link) {
        return repo.save(link);
    }

    @Override
    public Optional<ReservationTeamLink> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<ReservationTeamLink> findByTeamsFcMatchId(Long matchId) {
        return repo.findByTeamsFcMatchId(matchId);
    }

    @Override
    public Optional<ReservationTeamLink> findByReservationId(Long reservationId) {
        return repo.findByReservationId(reservationId);
    }

    @Override
    @Transactional
    public void delete(ReservationTeamLink link) {
        repo.delete(link);
    }
}
