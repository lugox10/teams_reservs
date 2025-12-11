package com.lugo.teams.reservs.application.dto.payment;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCheckoutDTO {
    private Long reservationId;
    private String paymentReference;   // id en gateway
    private String paymentUrl;         // url de pago (checkout)
    private BigDecimal amount;
    private String currency;
    private Instant expiresAt;
}
