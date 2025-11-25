package com.lugo.teams.reservs.infrastructure.persistence.adapters;

import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import com.lugo.teams.reservs.infrastructure.persistence.jpa.DataReservationRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    @Override
    public List<Reservation> findByUserNameOrderByStartDateTimeDesc(String userName) {
        return repo.findByUserNameOrderByStartDateTimeDesc(userName);
    }

    @Override
    public List<Reservation> findByFieldIdOrderByStartDateTime(Long fieldId) {
        return repo.findByFieldIdOrderByStartDateTime(fieldId);
    }

    @Override
    public List<Reservation> findOverlappingReservations(Long fieldId, LocalDateTime start, LocalDateTime end) {
        return repo.findOverlappingReservations(fieldId, start, end);
    }

    @Override
    public long countOverlappingReservations(Long fieldId, LocalDateTime start, LocalDateTime end) {
        return repo.countOverlappingReservations(fieldId, start, end);
    }

    @Override
    public List<Reservation> findUpcomingByUser(String userName, LocalDateTime from, Pageable pageable) {
        return repo.findUpcomingByUser(userName, from, pageable);
    }

    @Override
    public List<Reservation> findByVenueUpcoming(Long venueId, LocalDateTime from) {
        return repo.findByVenueUpcoming(venueId, from);
    }
}
