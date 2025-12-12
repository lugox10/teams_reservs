// package com.lugo.teams.reservs.application.mapper;
package com.lugo.teams.reservs.application.mapper;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.domain.model.Payment;
import com.lugo.teams.reservs.domain.model.PaymentStatus;
import com.lugo.teams.reservs.domain.model.Reservation;
import com.lugo.teams.reservs.domain.model.ReservationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class PaymentMapper {

    private static final Logger log = LoggerFactory.getLogger(PaymentMapper.class);

    /**
     * Aplica la información del callback (webhook) sobre la reserva y devuelve la reserva modificada.
     * No persiste: persistir en service/repo.
     */
    public Reservation applyPaymentCallback(Reservation reservation, PaymentCallbackDTO callback) {
        if (reservation == null) return null;
        if (callback == null) return reservation;

        // payment reference
        if (callback.getPaymentReference() != null) {
            reservation.setPaymentReference(callback.getPaymentReference());
        }

        // payment status (es enum en DTO) -> asignar directo
        PaymentStatus newStatus = callback.getPaymentStatus();
        if (newStatus != null) {
            reservation.setPaymentStatus(newStatus);
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
     * Crea una entidad Payment a partir del callback (útil para guardar el registro del pago).
     * - reservation: entidad existente (no nula)
     * - callback: DTO validado (no nulo)
     */
    public Payment toPaymentEntityFromCallback(Reservation reservation, PaymentCallbackDTO callback) {
        if (reservation == null || callback == null) return null;

        Payment p = new Payment();
        p.setReservation(reservation);
        p.setAmount(callback.getAmount() != null ? callback.getAmount() : reservation.getTotalAmount());
        p.setStatus(callback.getPaymentStatus() != null ? callback.getPaymentStatus() : PaymentStatus.PENDING);
        p.setPaymentReference(callback.getPaymentReference());
        p.setProvider(null); // provider lo setea el servicio integrador si aplica
        p.setRefund(false); // asumimos callback normal (no refund) — el servicio puede modificar si corresponde
        p.setMessage(null);
        return p;
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
