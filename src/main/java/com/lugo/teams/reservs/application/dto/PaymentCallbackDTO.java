    package com.lugo.teams.reservs.application.dto;

    import com.lugo.teams.reservs.domain.model.PaymentStatus;

    import jakarta.validation.constraints.NotNull;
    import lombok.*;

    import java.math.BigDecimal;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PaymentCallbackDTO {
        @NotNull
        private Long reservationId;

        @NotNull
        private PaymentStatus paymentStatus;

        private String paymentReference;

        private BigDecimal amount;
    }
