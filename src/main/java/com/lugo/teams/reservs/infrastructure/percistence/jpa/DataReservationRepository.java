package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DataReservationRepository extends JpaRepository<Reservation, Long> {

    // =========================
    // USER
    // =========================

    List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName);

    @Query("""
    SELECT r FROM Reservation r
    WHERE r.userName = :userName
      AND r.endDateTime >= :from
    ORDER BY r.startDateTime
""")
    Page<Reservation> findUpcomingByUser(
            @Param("userName") String userName,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );


    @Query("""
        SELECT r FROM Reservation r
        WHERE r.reservUser.id = :userId
          AND r.venue.id = :venueId
        ORDER BY r.createdAt DESC
    """)
    Optional<Reservation> findLastReservationByUserAndVenue(
            @Param("userId") Long userId,
            @Param("venueId") Long venueId
    );

    // =========================
    // FIELD / AVAILABILITY
    // =========================

    List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.field.id = :fieldId
          AND r.startDateTime < :end
          AND r.endDateTime > :start
          AND r.status <> com.lugo.teams.reservs.domain.model.ReservationStatus.CANCELLED
        ORDER BY r.startDateTime
    """)
    List<Reservation> findOverlappingReservations(
            @Param("fieldId") Long fieldId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.field.id = :fieldId
          AND r.startDateTime < :end
          AND r.endDateTime > :start
          AND r.status <> com.lugo.teams.reservs.domain.model.ReservationStatus.CANCELLED
    """)
    long countOverlappingReservations(
            @Param("fieldId") Long fieldId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // =========================
    // OWNER DASHBOARD (ÚNICO MÉTODO)
    // =========================

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.venue.id IN :venueIds
          AND r.startDateTime >= :from
          AND r.startDateTime < :to
        ORDER BY r.startDateTime DESC
    """)
    Page<Reservation> findByVenueIdsAndDateRange(
            @Param("venueIds") List<Long> venueIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
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


    Page<Reservation> findByVenue_IdInAndStatusInAndStartDateTimeBetween(
            List<Long> venueIds,
            Collection<ReservationStatus> statuses,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);




    @Query(value = """
    SELECT r.venue_id AS venueId,
           COUNT(*) AS totalReservations,
           COALESCE(SUM(r.total_amount),0) AS totalRevenue
    FROM reservations r
    WHERE r.venue_id IN :venueIds
      AND r.start_date_time >= :from
      AND r.start_date_time < :to
    GROUP BY r.venue_id
""", nativeQuery = true)
    List<Object[]> countAndRevenueByVenueNative(
            @Param("venueIds") List<Long> venueIds,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );


}
