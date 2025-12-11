package com.lugo.teams.reservs.infrastructure.percistence.jpa;

import com.lugo.teams.reservs.domain.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DataPaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentReference(String paymentReference);
    List<Payment> findByReservationId(Long reservationId);
}

