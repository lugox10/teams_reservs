package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.TeamsMatchDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio principal para crear / obtener / cancelar reservas.
 */
public interface ReservationService {

    /**
     * Crea una reserva (valida solapamientos, calcula monto, persiste).
     * Devuelve DTO con info y estado inicial (PENDING o CONFIRMED si pago inmediato).
     */
    ReservationResponseDTO createReservation(ReservationRequestDTO request);

    /**
     * Confirma una reserva (pago o validación por owner).
     */
    ReservationResponseDTO confirmReservation(Long reservationId, String paymentReference);

    /**
     * Cancela una reserva (por usuario o admin/owner).
     */
    boolean cancelReservation(Long reservationId, String cancelledByUsername);

    /**
     * Busca reservas de un usuario (historial / próximas).
     */
    List<ReservationResponseDTO> findByUser(String userName);

    /**
     * Buscar reservas próximas por venue.
     */
    List<ReservationResponseDTO> findUpcomingByVenue(Long venueId);

    /**
     * Comprueba disponibilidad para un campo entre dos instantes.
     */
    boolean isAvailable(Long fieldId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * (Opcional) Intención de crear partido en Teams-FC: crear match si owner/usuario lo solicita
     * y si la reserva está confirmada. Devuelve info del match creado o vacío si no se creó.
     */
    Optional<TeamsMatchDTO> createTeamsFcMatchIfRequested(Long reservationId);

    Optional<ReservationResponseDTO> findById(Long reservationId);

    com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO initiatePayment(Long reservationId, BigDecimal amount, boolean pagoParcial);
    void handlePaymentCallback(PaymentCallbackDTO callback);
    boolean refundPayment(String paymentReference, BigDecimal amount);

}
