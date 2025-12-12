
package com.lugo.teams.reservs.application.dto.payment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCheckoutDTO {
    @NotNull(message = "reservationId es obligatorio")
    private Long reservationId;

    private String paymentReference;   // id en gateway (puede venir o generarse)

    private String paymentUrl;         // url de pago (checkout)

    @NotNull(message = "amount es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount debe ser mayor que 0")
    private BigDecimal amount;

    private String currency;

    private Instant expiresAt;
}
