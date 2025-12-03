package com.lugo.teams.reservs.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO sencillo que representa el partido que Teams-FC devuelve al módulo de reservas.
 * Usar como contrato mínimo entre monolitos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamsMatchDTO {
    private Long id;
    private LocalDate fecha;
    private String jornada;
    private String estado;      // p.ej. "SCHEDULED", "FINISHED"
    private String url;         // link público al partido en Teams-FC (si aplica)
    private Integer jugadoresConfirmados;
}
