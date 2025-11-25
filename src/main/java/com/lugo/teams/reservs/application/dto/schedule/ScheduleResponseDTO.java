package com.lugo.teams.reservs.application.dto.schedule;

import lombok.Data;

@Data
public class ScheduleResponseDTO {
    private Long id;
    private Long venueId;
    private String horaInicio;
    private String horaFin;
    private Double precio;
}
