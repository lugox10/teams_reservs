package com.lugo.teams.reservs.infrastructure.persistence.adapters;

import com.lugo.teams.reservs.domain.model.TimeSlot;
import com.lugo.teams.reservs.domain.repository.TimeSlotRepository;
import com.lugo.teams.reservs.infrastructure.persistence.jpa.DataTimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public class JpaTimeSlotRepositoryAdapter implements TimeSlotRepository {

    private final DataTimeSlotRepository repo;

    public JpaTimeSlotRepositoryAdapter(DataTimeSlotRepository repo) {
        this.repo = repo;
    }

    @Override
    public TimeSlot save(TimeSlot slot) {
        return repo.save(slot);
    }

    @Override
    public Optional<TimeSlot> findById(Long id) {
        return repo.findById(id);
    }

    @Override
    public List<TimeSlot> findByFieldIdAndAvailableTrue(Long fieldId) {
        return repo.findByFieldIdAndAvailableTrue(fieldId);
    }

    @Override
    public List<TimeSlot> findAvailableByFieldBetween(Long fieldId, LocalDateTime from, LocalDateTime to) {
        return repo.findAvailableByFieldBetween(fieldId, from, to);
    }

    @Override
    public List<TimeSlot> findOverlappingSlots(Long fieldId, LocalDateTime start, LocalDateTime end) {
        return repo.findOverlappingSlots(fieldId, start, end);
    }
}
