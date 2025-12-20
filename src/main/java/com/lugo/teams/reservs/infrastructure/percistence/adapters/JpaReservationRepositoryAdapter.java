package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class JpaReservationRepositoryAdapter implements ReservationRepository {

    private final DataReservationRepository repo;

    public JpaReservationRepositoryAdapter(DataReservationRepository repo) {
        this.repo = repo;
    }

    @Override
    public Reservation save(Reservation reservation) {
        return repo.save(reservation);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public void delete(Reservation reservation) {
        repo.delete(reservation);
    }

    // USER

    @Override
    public List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName) {
        return repo.findByUserNameOrderByStartDateTimeDesc(userName);
    }

    @Override
    public Page<Reservation> findUpcomingByUser(
            String userName,
            LocalDateTime from,
            Pageable pageable
    ) {
        return repo.findUpcomingByUser(userName, from, pageable);
    }


    @Override
    public Optional<Reservation> findLastByUserAndVenue(Long userId, Long venueId) {
        return repo.findLastReservationByUserAndVenue(userId, venueId);
    }

    // FIELD

    @Override
    public List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId) {
        return repo.findByFieldIdOrderByStartDateTime(fieldId);
    }

    @Override
    public List<Reservation> findOverlappingReservations(
            Long fieldId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return repo.findOverlappingReservations(fieldId, start, end);
    }

    @Override
    public long countOverlappingReservations(
            Long fieldId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return repo.countOverlappingReservations(fieldId, start, end);
    }

    // OWNER DASHBOARD 🔥

    @Override
    public Page<Reservation> findByVenueIdsAndDateRange(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return repo.findByVenueIdsAndDateRange(venueIds, from, to, pageable);
    }

    @Override
    public List<Reservation> findByVenueIdInAndStartDateTimeBetween(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return repo.findByVenueIdInAndStartDateTimeBetween(venueIds, from, to);
    }

    @Override
    public Page<Reservation> findByVenueIdInAndStartDateTimeBetween(
            List<Long> venueIds,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable
    ) {
        return repo.findByVenueIdInAndStartDateTimeBetween(venueIds, from, to, pageable);
    }

    @Override
    public Page<Reservation> findByVenue_IdInAndStatusInAndStartDateTimeBetween(
            List<Long> venueIds,
            Collection<ReservationStatus> statuses,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        return repo.findByVenue_IdInAndStatusInAndStartDateTimeBetween(
                venueIds,
                statuses,
                start,
                end,
                pageable
        );
    }




}
