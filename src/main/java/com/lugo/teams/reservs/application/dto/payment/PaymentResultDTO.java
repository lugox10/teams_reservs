
package com.lugo.teams.reservs.application.dto.payment;

import com.lugo.teams.reservs.domain.model.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para la iniciación del pago / checkout y resultados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultDTO {

    private Long reservationId;
    private String checkoutUrl;
    private String paymentReference;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime expiresAt;
    private String message;
}
