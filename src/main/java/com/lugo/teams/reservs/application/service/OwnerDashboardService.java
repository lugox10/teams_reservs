package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Consultas y métricas que necesita el dueño (dashboard).
 */
public interface OwnerDashboardService {

    /**
     * Lista reservas de un owner por rango de fechas (todas sus sedes).
     */
    List<ReservationResponseDTO> findReservationsByOwner(Long ownerId, LocalDate from, LocalDate to);

    /**
     * Resumen de ingresos por sede en un mes.
     * key = venueId, value = total cobrado
     */
    Map<Long, Double> getMonthlyRevenueByVenue(Long ownerId, int year, int month);

    /**
     * Métricas rápidas: reservas totales, ocupación promedio, ingresos.
     */
    Map<String, Object> getOwnerOverviewMetrics(Long ownerId, LocalDate from, LocalDate to);

    Long getOwnerIdFromAuth(Authentication auth);


    Page<ReservationResponseDTO> findReservationsByOwner(
            Long ownerId,
            LocalDate from,
            LocalDate to,
                Pageable pageable
    );

    Map<Long, Map<String, Object>> getMetricsByVenue(Long ownerId, LocalDate from, LocalDate to);
}
