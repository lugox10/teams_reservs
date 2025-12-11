package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.PaymentStatus;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class PaymentMapper {

    private static final Logger log = LoggerFactory.getLogger(PaymentMapper.class);

    /**
     * Aplica la información de callback (webhook) sobre la reserva.
     * No persiste — deja eso al servicio/repository.
     */
    public Reservation applyPaymentCallback(Reservation reservation, PaymentCallbackDTO callback) {
        if (reservation == null) return null;
        if (callback == null) return reservation;

        // payment reference
        if (callback.getPaymentReference() != null) {
            reservation.setPaymentReference(callback.getPaymentReference());
        }

        // payment status -> si callback trae String en vez de enum, evitar NPE (si aplica)
        try {
            if (callback.getPaymentStatus() != null) {
                // si getPaymentStatus() ya es PaymentStatus, asigna directo; si es String, hay que convertir
                if (callback.getPaymentStatus() instanceof PaymentStatus) {
                    reservation.setPaymentStatus((PaymentStatus) callback.getPaymentStatus());
                } else {
                    // fallback si viene como String (compatible con algunos webhooks)
                    try {
                        reservation.setPaymentStatus(PaymentStatus.valueOf(callback.getPaymentStatus().toString()));
                    } catch (Exception e) {
                        log.warn("No se pudo convertir paymentStatus del callback: {}", callback.getPaymentStatus());
                    }
                }
            }
        } catch (ClassCastException e) {
            log.warn("Tipo inesperado en callback.getPaymentStatus(): {}", e.getMessage());
        }

        // ajustar totalAmount si viene y es distinto
        if (callback.getAmount() != null) {
            BigDecimal cb = callback.getAmount();
            BigDecimal current = reservation.getTotalAmount();
            if (current == null || cb.compareTo(current) != 0) {
                reservation.setTotalAmount(cb);
            }
        }

        // si pago confirmado, marcar reserva como CONFIRMED
        if (reservation.getPaymentStatus() == PaymentStatus.PAID) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
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
