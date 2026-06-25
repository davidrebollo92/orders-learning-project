package com.amazon.payment_service.payment.infrastructure.gateway;

import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000");

    @Override
    public Payment process(Payment payment) {
        if (payment.getAmount().amount().compareTo(MAX_AMOUNT) > 0) {
            return payment.fail();
        }

        return payment.pay(UUID.randomUUID());
    }
}
