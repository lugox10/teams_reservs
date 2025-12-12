
package com.lugo.teams.reservs.application.dto.payment;

import com.lugo.teams.reservs.domain.model.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentCallbackDTO {

    @NotNull(message = "reservationId es obligatorio")
    private Long reservationId;

    @NotNull(message = "paymentStatus es obligatorio")
    private PaymentStatus paymentStatus;

    private String paymentReference;

    @DecimalMin(value = "0.0", inclusive = false, message = "amount debe ser mayor que 0")
    private BigDecimal amount;
}
