package com.lugo.teams.reservs.application.dto.schedule;

import lombok.*;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
    private Long id;
    private Long venueId;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Double precio;
}
