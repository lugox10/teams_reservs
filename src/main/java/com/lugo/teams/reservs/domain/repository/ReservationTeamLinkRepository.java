package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import java.util.List;
import java.util.Optional;

public interface ReservationTeamLinkRepository {
    ReservationTeamLink save(ReservationTeamLink link);
    Optional<ReservationTeamLink> findById(Long id);
    List<ReservationTeamLink> findByTeamsFcMatchId(Long matchId);
    Optional<ReservationTeamLink> findByReservationId(Long reservationId);
    void delete(ReservationTeamLink link);
}
