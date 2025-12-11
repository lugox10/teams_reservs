package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.teamsfc.TeamsMatchDTO;
import com.lugo.teams.reservs.domain.model.ReservationTeamLink;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TeamsMatchMapper {

    /**
     * Mapea TeamsMatchDTO (id, fecha, jornada, estado, url, jugadoresConfirmados)
     * a ReservationTeamLink. Como el DTO no trae home/away, uso 'jornada' o id para teamName.
     * Si quieres almacenar home/away explícito, añade esos campos al DTO.
     */
    public ReservationTeamLink toReservationTeamLink(TeamsMatchDTO dto) {
        if (dto == null) return null;
        ReservationTeamLink link = new ReservationTeamLink();
        link.setTeamsFcMatchId(dto.getId());      // id del DTO -> teamsFcMatchId
        link.setTeamsFcUrl(dto.getUrl());         // url -> teamsFcUrl

        // Construyo un teamName razonable desde lo disponible:
        String teamName = null;
        if (dto.getJornada() != null && !dto.getJornada().isBlank()) {
            teamName = dto.getJornada();
        } else if (dto.getFecha() != null) {
            teamName = "Match-" + dto.getId() + "@" + dto.getFecha();
        } else {
            teamName = "Match-" + dto.getId();
        }
        link.setTeamName(teamName);

        link.setCreatedAt(LocalDateTime.now());
        return link;
    }

    public TeamsMatchDTO toDTO(ReservationTeamLink link) {
        if (link == null) return null;
        TeamsMatchDTO dto = TeamsMatchDTO.builder().build();
        dto.setId(link.getTeamsFcMatchId());
        dto.setUrl(link.getTeamsFcUrl());

        // intentar recuperar jornada/fecha si teamName tiene info (heurístico)
        String teamName = link.getTeamName();
        if (teamName != null) {
            dto.setJornada(teamName);
        }
        // no podemos poblar fecha/jugadoresConfirmados desde ReservationTeamLink sin campos adicionales
        return dto;
    }
}
