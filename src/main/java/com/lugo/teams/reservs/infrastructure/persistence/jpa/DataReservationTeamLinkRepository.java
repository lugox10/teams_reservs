package com.lugo.teams.reservs.infrastructure.persistence.jpa;

import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataReservationTeamLinkRepository extends JpaRepository<ReservationTeamLink, Long> {
}
