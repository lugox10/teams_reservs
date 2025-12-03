package com.lugo.teams.reservs.infrastructure.persistence.jpa;

import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataReservationTeamLinkRepository extends JpaRepository<ReservationTeamLink, Long> {
    List<ReservationTeamLink> findByTeamsFcMatchId(Long matchId);
    Optional<ReservationTeamLink> findByReservationId(Long reservationId);
}
