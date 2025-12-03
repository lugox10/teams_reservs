package com.lugo.teams.reservs.application.dto.schedule;

import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRequestDTO {
    @NotNull
    private Long venueId;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    private Double precio;
}
