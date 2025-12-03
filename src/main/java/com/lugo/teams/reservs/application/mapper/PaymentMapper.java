package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    /**
     * Aplica la información de callback (webhook) sobre la reserva.
     * No persiste — deja eso al servicio/repository.
     */
    public Reservation applyPaymentCallback(Reservation reservation, PaymentCallbackDTO callback) {
        if (reservation == null || callback == null) return reservation;
        reservation.setPaymentReference(callback.getPaymentReference());
        reservation.setPaymentStatus(callback.getPaymentStatus());
        // si amount viene, ajustamos el totalAmount si aún era null
        if (callback.getAmount() != null) {
            if (reservation.getTotalAmount() == null || reservation.getTotalAmount().compareTo(callback.getAmount()) != 0) {
                reservation.setTotalAmount(callback.getAmount());
            }
        }
        // si pago confirmado, marcar reserva como CONFIRMED
        if (callback.getPaymentStatus() == PaymentStatus.PAID) {
            reservation.setStatus(com.lugo.teams.reservs.domain.model.ReservationStatus.CONFIRMED);
        }
        return reservation;
    }

    /**
     * Construye un PaymentResultDTO con datos para el checkout (respuesta de initiatePayment).
     */
    public PaymentResultDTO toPaymentResultDto(Long reservationId, String checkoutUrl, String paymentReference,
                                               BigDecimal amount, PaymentStatus status, LocalDateTime expiresAt, String message) {
        return PaymentResultDTO.builder()
                .reservationId(reservationId)
                .checkoutUrl(checkoutUrl)
                .paymentReference(paymentReference)
                .amount(amount)
                .status(status)
                .expiresAt(expiresAt)
                .message(message)
                .build();
    }
}
