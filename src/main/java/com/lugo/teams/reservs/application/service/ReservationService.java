package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.reserv.ReservationResponseDTO;
import com.lugo.teams.reservs.application.dto.teamsfc.TeamsMatchDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationService {
    ReservationResponseDTO createReservation(ReservationRequestDTO request);
    ReservationResponseDTO confirmReservation(Long reservationId, String paymentReference);
    boolean cancelReservation(Long reservationId, String cancelledByUsername);
    List<ReservationResponseDTO> findByUser(String userName);
    List<ReservationResponseDTO> findUpcomingByVenue(Long venueId);
    boolean isAvailable(Long fieldId, LocalDateTime start, LocalDateTime end);
    Optional<TeamsMatchDTO> createTeamsFcMatchIfRequested(Long reservationId);
    Optional<ReservationResponseDTO> findById(Long reservationId);

    // agregado: actualizar reserva
    ReservationResponseDTO updateReservation(Long id, ReservationRequestDTO dto);

    PaymentResultDTO initiatePayment(Long reservationId, BigDecimal amount, boolean pagoParcial);
    void handlePaymentCallback(PaymentCallbackDTO callback);
    boolean refundPayment(String paymentReference, BigDecimal amount);

    List<ReservationResponseDTO> findByUserUpcoming(String userName, LocalDateTime from, org.springframework.data.domain.Pageable pageable);
    List<ReservationResponseDTO> findByField(Long fieldId);
}
