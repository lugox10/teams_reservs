package com.lugo.teams.reservs.infrastructure.persistence.adapters;



import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import com.lugo.teams.reservs.domain.repository.ReservationTeamLinkRepository;
import com.lugo.teams.reservs.infrastructure.persistence.jpa.DataReservationTeamLinkRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaReservationTeamLinkRepository implements ReservationTeamLinkRepository {

    private final DataReservationTeamLinkRepository dataRepo;

    public JpaReservationTeamLinkRepository(DataReservationTeamLinkRepository dataRepo) {
        this.dataRepo = dataRepo;
    }

    @Override public ReservationTeamLink save(ReservationTeamLink link) { return dataRepo.save(link); }
    @Override public Optional<ReservationTeamLink> findById(Long id) { return dataRepo.findById(id); }

    @Override public List<ReservationTeamLink> findByTeamsFcMatchId(Long matchId) { return dataRepo.findByTeamsFcMatchId(matchId); }
    @Override public void delete(ReservationTeamLink link) { dataRepo.delete(link); }

    @Override
    public ReservationTeamLink findByReservationId(Long reservationId) {
        return (ReservationTeamLink) dataRepo.findByReservationId(reservationId);
    }
}
