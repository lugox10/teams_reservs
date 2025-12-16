package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.Field;
import com.lugo.teams.reservs.domain.repository.FieldRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataFieldRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class JpaFieldRepositoryAdapter implements FieldRepository {

    private final DataFieldRepository repo;

    public JpaFieldRepositoryAdapter(DataFieldRepository repo) {
        this.repo = repo;
    }

    @Override
    public Field save(Field field) {
        return repo.save(field);
    }

    @Override
    public Optional<Field> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<Field> findByVenueId(Long venueId) {
        return repo.findByVenueId(venueId);
    }

    @Override
    public List<Field> findByActiveTrue() {
        return repo.findByActiveTrue();
    }



    @Override
    public Optional<Field> findWithDetailsById(Long id) {
        return repo.findWithDetailsById(id);
    }

    @Override
    public List<LocalTime> findBookedStartTimesForDate(Field field, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        List<LocalDateTime> booked = repo.findBookedStartDateTimesBetween(field, start, end);
        return booked.stream().map(LocalDateTime::toLocalTime).toList();
    }



}
