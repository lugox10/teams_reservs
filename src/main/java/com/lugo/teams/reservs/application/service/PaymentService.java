package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;


import java.math.BigDecimal;

/**
 * Abstracción para iniciar pagos / recibir callbacks.
 * Implementación usará cliente de gateway (Feign/RestTemplate).
 */
public interface PaymentService {

    /**
     * Inicia el pago por una reserva y devuelve un objeto con datos para redirigir al checkout
     * (url, reference, monto).
     */
    com.lugo.teams.reservs.application.dto.payment.PaymentResultDTO initiatePayment(Long reservationId, BigDecimal amount, boolean pagoParcial);

    /**
     * Procesa callback del gateway (webhook). Actualiza estado de la reserva.
     */
    void handlePaymentCallback(PaymentCallbackDTO callback);

    /**
     * Intenta reembolsar una reserva (si aplica).
     */
    boolean refundPayment(String paymentReference, BigDecimal amount);
}
