package com.lugo.teams.reservs.domain.repository;

import com.lugo.teams.reservs.domain.model.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByPaymentReference(String ref);
    List<Payment> findByReservationId(Long reservationId);
}

