package com.lugo.teams.reservs.application.dto.schedule;

import lombok.Data;
import java.time.LocalTime;

@Data
public class ScheduleRequestDTO {
    private Long venueId;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Double precio;
}
