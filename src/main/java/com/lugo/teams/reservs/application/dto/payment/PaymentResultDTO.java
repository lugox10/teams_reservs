package com.lugo.teams.reservs.application.dto.payment;

import com.lugo.teams.reservs.domain.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {
    /**
     * Id de la reserva asociada (eco del request)
     */
    private Long reservationId;

    /**
     * URL para redirigir al checkout (si aplica)
     */
    private String checkoutUrl;

    /**
     * Referencia única del pago generada por el gateway (o por nuestro sistema)
     */
    private String paymentReference;

    /**
     * Monto a cobrar/pendiente
     */
    private BigDecimal amount;

    /**
     * Estado inicial devuelto por el gateway (PENDING, PAID, FAILED...)
     */
    private PaymentStatus status;

    /**
     * Timestamp opcional de expiración del checkout / sesión de pago
     */
    private LocalDateTime expiresAt;

    /**
     * Mensaje técnico opcional (para logs/debug)
     */
    private String message;
}
