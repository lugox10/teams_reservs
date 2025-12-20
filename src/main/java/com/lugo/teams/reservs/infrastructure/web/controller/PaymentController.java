package com.lugo.teams.reservs.infrastructure.web.controller;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO;
import com.lugo.teams.reservs.application.service.PaymentCallbackService;
import com.lugo.teams.reservs.application.service.PaymentService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentCallbackService paymentCallbackService;

    /**
     * Inicia el checkout para una reserva.
     * Body: { "reservationId": 123, "amount": 50000.00, "pagoParcial": false }
     */
    @PostMapping("/checkout")
    public ResponseEntity<PaymentResultDTO> initiateCheckout(@RequestBody @Valid PaymentCheckoutRequest req) {
        log.info("Iniciando checkout reservation={} amount={} parcial={}", req.getReservationId(), req.getAmount(), req.isPagoParcial());
        PaymentResultDTO result = paymentService.initiatePayment(req.getReservationId(), req.getAmount(), req.isPagoParcial());
        return ResponseEntity.ok(result);
    }

    /**
     * Webhook público que el gateway llamará.
     * Validá firma en X-Signature si tu gateway la provee (TODO).
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> paymentCallback(
            @RequestHeader(value = "X-Signature", required = false) String signature,
            @RequestBody @Valid PaymentCallbackDTO callback
    ) {
        log.info("Callback recibido reservation={} ref={} status={}", callback.getReservationId(), callback.getPaymentReference(), callback.getPaymentStatus());

        // TODO: validar signature si el gateway la envía.
        // if (!verifySignature(signature, rawBody)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        paymentCallbackService.handleCallback(callback);
        return ResponseEntity.ok().build();
    }

    // ---------------- DTOs ----------------
    @Data
    public static class PaymentCheckoutRequest {
        private Long reservationId;
        private BigDecimal amount;
        private boolean pagoParcial = false;
    }
}
