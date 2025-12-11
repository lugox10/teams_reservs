package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.application.mapper.PaymentMapper;
import com.lugo.teams.reservs.application.service.PaymentService;
import com.lugo.teams.reservs.domain.model.Payment;
import com.lugo.teams.reservs.domain.model.PaymentStatus;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.repository.PaymentRepository;
import com.lugo.teams.reservs.domain.repository.ReservationRepository;
import com.lugo.teams.reservs.shared.exception.NotFoundException;
import com.lugo.teams.reservs.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResultDTO initiatePayment(Long reservationId, BigDecimal amount, boolean pagoParcial) {
        if (reservationId == null) throw new BadRequestException("reservationId es requerido");

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reserva no encontrada: " + reservationId));

        if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
            return PaymentResultDTO.builder()
                    .reservationId(reservationId)
                    .status(PaymentStatus.PAID)
                    .message("La reserva ya está pagada, crack")
                    .build();
        }

        BigDecimal toPay = (amount != null) ? amount : (reservation.getTotalAmount() != null ? reservation.getTotalAmount() : BigDecimal.ZERO);
        String reference = "PAY-" + UUID.randomUUID();

        // Create Payment record (linked to reservation)
        Payment p = Payment.builder()
                .reservation(reservation)
                .amount(toPay)
                .status(PaymentStatus.PENDING)
                .paymentReference(reference)
                .build();
        // set auditing times (BaseEntity has setters)
        p.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        // Update reservation if full payment intent (or store ref)
        if (!pagoParcial) {
            reservation.setPaymentReference(reference);
            reservation.setPaymentStatus(PaymentStatus.PENDING);
            reservationRepository.save(reservation);
        }

        String checkoutUrl = "https://fake-checkout.example/checkout/" + reference;

        log.info("Iniciado pago reservation={} ref={} amount={}", reservationId, reference, toPay);
        return paymentMapper.toPaymentResultDto(reservationId, checkoutUrl, reference, toPay, p.getStatus(), LocalDateTime.now().plusMinutes(30), "Checkout iniciado");
    }

    @Override
    @Transactional
    public void handlePaymentCallback(PaymentCallbackDTO callback) {
        if (callback == null || callback.getPaymentReference() == null) {
            log.warn("Callback inválido");
            return;
        }

        Optional<Payment> pOpt = paymentRepository.findByPaymentReference(callback.getPaymentReference());
        if (pOpt.isEmpty()) {
            log.warn("Payment no encontrado para referencia {}", callback.getPaymentReference());
            return;
        }

        Payment p = pOpt.get();
        if (p.getStatus() == callback.getPaymentStatus()) {
            log.info("Callback idempotente para payment {}", p.getId());
            return;
        }

        p.setStatus(callback.getPaymentStatus());
        if (callback.getAmount() != null) p.setAmount(callback.getAmount());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        if (p.getReservation() != null && p.getReservation().getId() != null) {
            reservationRepository.findById(p.getReservation().getId()).ifPresent(reservation -> {
                paymentMapper.applyPaymentCallback(reservation, callback);
                reservationRepository.save(reservation);
            });
        } else {
            log.warn("Payment {} no tiene reserva asociada al procesar callback", p.getPaymentReference());
        }
    }

    @Override
    @Transactional
    public boolean refundPayment(String paymentReference, BigDecimal amount) {
        if (paymentReference == null || paymentReference.isBlank()) throw new BadRequestException("paymentReference es requerido");

        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new NotFoundException("Payment no encontrado: " + paymentReference));

        // Lógica mínima: marcar REFUNDED localmente
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (payment.getReservation() != null && payment.getReservation().getId() != null) {
            reservationRepository.findById(payment.getReservation().getId()).ifPresent(reservation -> {
                reservation.setPaymentStatus(PaymentStatus.REFUNDED);
                reservationRepository.save(reservation);
            });
        }
        return true;
    }
}
