package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.ReservationRequestDTO;
import com.lugo.teams.reservs.application.dto.ReservationResponseDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationService {

    ReservationResponseDTO createReservation(ReservationRequestDTO request);

    Optional<ReservationResponseDTO> findById(Long id);

    List<ReservationResponseDTO> findByUser(String userName, Pageable pageable);

    List<ReservationResponseDTO> findByVenueUpcoming(Long venueId, LocalDateTime from);

    /**
     * Cancela la reserva si aplica. Devuelve true si se canceló correctamente.
     */
    boolean cancelReservation(Long reservationId, String requestedByUserName);

    /**
     * Marcar pago confirmado: actualiza paymentStatus y status.
     * Si createTeamsMatch true y hay integración, puede crear un match/link.
     */
    ReservationResponseDTO confirmPayment(Long reservationId, String paymentReference, boolean createTeamsMatch);
}
