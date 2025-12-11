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

    @Query("select r from Reservation r " +
            "where r.field.id = :fieldId " +
            "and r.status <> 'CANCELLED' " +
            "and not (r.endDateTime <= :start or r.startDateTime >= :end)")
    List<Reservation> findOverlappingReservations(@Param("fieldId") Long fieldId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Query("select count(r) from Reservation r " +
            "where r.field.id = :fieldId " +
            "and r.status <> 'CANCELLED' " +
            "and not (r.endDateTime <= :start or r.startDateTime >= :end)")
    long countOverlappingReservations(@Param("fieldId") Long fieldId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("select r from Reservation r " +
            "where r.userName = :userName " +
            "and r.endDateTime >= :from " +
            "order by r.startDateTime")
    List<Reservation> findUpcomingByUser(@Param("userName") String userName,
                                         @Param("from") LocalDateTime from,
                                         Pageable pageable);

    @Query("select r from Reservation r " +
            "where r.field.venue.id = :venueId " +
            "and r.startDateTime >= :from " +
            "order by r.startDateTime")
    List<Reservation> findByVenueUpcoming(@Param("venueId") Long venueId,
                                          @Param("from") LocalDateTime from);
}
