package com.lugo.teams.reservs.application.service;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;

public interface PaymentCallbackService {
    /**
     * Procesa el callback/webhook del gateway.
     * Debe ser idempotente.
     */
    void handleCallback(PaymentCallbackDTO callback);
}
