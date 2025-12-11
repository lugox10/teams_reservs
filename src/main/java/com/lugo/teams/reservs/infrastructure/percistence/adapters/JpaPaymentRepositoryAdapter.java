package com.lugo.teams.reservs.infrastructure.percistence.adapters;

import com.lugo.teams.reservs.domain.model.Payment;
import com.lugo.teams.reservs.domain.repository.PaymentRepository;
import com.lugo.teams.reservs.infrastructure.percistence.jpa.DataPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaPaymentRepositoryAdapter implements PaymentRepository {

    private final DataPaymentRepository dataRepo;

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return dataRepo.save(payment);
    }

    @Override
    public Optional<Payment> findByPaymentReference(String ref) {
        if (ref == null || ref.isBlank()) return Optional.empty();
        return dataRepo.findByPaymentReference(ref);
    }

    @Override
    public List<Payment> findByReservationId(Long reservationId) {
        if (reservationId == null) return List.of();
        return dataRepo.findByReservationId(reservationId);
    }
}
