package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.domain.model.Reservation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DataReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName);

    List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.field.id = :fieldId " +
            "  AND r.startDateTime < :end " +
            "  AND r.endDateTime > :start " +
            "  AND r.status <> com.lugo.teams.reservs.domain.model.ReservationStatus.CANCELLED " +
            "ORDER BY r.startDateTime")
    List<Reservation> findOverlappingReservations(@Param("fieldId") Long fieldId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(r) FROM Reservation r " +
            "WHERE r.field.id = :fieldId " +
            "  AND r.startDateTime < :end " +
            "  AND r.endDateTime > :start " +
            "  AND r.status <> com.lugo.teams.reservs.domain.model.ReservationStatus.CANCELLED")
    long countOverlappingReservations(@Param("fieldId") Long fieldId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.userName = :userName " +
            "  AND r.endDateTime >= :from " +
            "ORDER BY r.startDateTime")
    List<Reservation> findUpcomingByUser(@Param("userName") String userName,
                                         @Param("from") LocalDateTime from,
                                         Pageable pageable);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.field.venue.id = :venueId " +
            "  AND r.startDateTime >= :from " +
            "ORDER BY r.startDateTime")
    List<Reservation> findByVenueUpcoming(@Param("venueId") Long venueId,
                                          @Param("from") LocalDateTime from);


}
