package com.lugo.teams.reservs.application.service.impl;

import com.lugo.teams.reservs.application.dto.payment.PaymentCallbackDTO;
import com.lugo.teams.reservs.application.service.PaymentCallbackService;
import com.lugo.teams.reservs.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentCallbackServiceImpl implements PaymentCallbackService {

    private final PaymentService paymentService;

    @Override
    public void handleCallback(PaymentCallbackDTO callback) {
        // delega al PaymentService (idem, idempotencia en PaymentServiceImpl)
        paymentService.handlePaymentCallback(callback);
    }
}
