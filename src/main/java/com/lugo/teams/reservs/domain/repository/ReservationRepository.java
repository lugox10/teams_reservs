package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Reservation;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
    void delete(Reservation reservation);

    List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName);
    List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId);

    List<Reservation> findOverlappingReservations(Long fieldId, LocalDateTime start, LocalDateTime end);
    long countOverlappingReservations(Long fieldId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findUpcomingByUser(String userName, LocalDateTime from, Pageable pageable);
    List<Reservation> findByVenueUpcoming(Long venueId, LocalDateTime from);
    List<Reservation> findByUserNameAndStartDateTimeBetween(String userName, LocalDateTime from, LocalDateTime to);

}
