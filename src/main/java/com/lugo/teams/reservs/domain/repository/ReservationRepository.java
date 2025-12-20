package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void delete(Reservation reservation);

    // USER
    List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName);

    Page<Reservation> findUpcomingByUser(
            String userName,
            LocalDateTime from,
            Pageable pageable
    );

    Optional<Reservation> findLastByUserAndVenue(Long userId, Long venueId);

    // FIELD
    List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId);

    List<Reservation> findOverlappingReservations(
            Long fieldId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countOverlappingReservations(
            Long fieldId,
            LocalDateTime start,
            LocalDateTime end
    );

    // OWNER DASHBOARD 🔥
    Page<Reservation> findByVenueIdsAndDateRange(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    List<Reservation> findByVenueIdInAndStartDateTimeBetween(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to
    );

    Page<Reservation> findByVenueIdInAndStartDateTimeBetween(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"venue", "field"})
    Page<Reservation> findByVenue_IdInAndStatusInAndStartDateTimeBetween(
            List<Long> venueIds,
            Collection<ReservationStatus> statuses,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );






}
