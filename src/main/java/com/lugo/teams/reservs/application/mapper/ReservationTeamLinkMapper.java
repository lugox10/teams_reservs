package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.reserv.ReservationTeamLinkDTO;
import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import org.springframework.stereotype.Component;

@Component
public class ReservationTeamLinkMapper {

    public ReservationTeamLinkDTO toDTO(ReservationTeamLink e) {
        if (e == null) return null;
        return ReservationTeamLinkDTO.builder()
                .id(e.getId())
                .teamsFcMatchId(e.getTeamsFcMatchId())
                .teamsFcUrl(e.getTeamsFcUrl())
                .teamName(e.getTeamName())
                .createdAt(e.getCreatedAt())
                .build();
    }

    public ReservationTeamLink toEntity(ReservationTeamLinkDTO dto) {
        if (dto == null) return null;
        ReservationTeamLink e = new ReservationTeamLink();
        e.setId(dto.getId());
        e.setTeamsFcMatchId(dto.getTeamsFcMatchId());
        e.setTeamsFcUrl(dto.getTeamsFcUrl());
        e.setTeamName(dto.getTeamName());
        e.setCreatedAt(dto.getCreatedAt());
        return e;
    }
}
