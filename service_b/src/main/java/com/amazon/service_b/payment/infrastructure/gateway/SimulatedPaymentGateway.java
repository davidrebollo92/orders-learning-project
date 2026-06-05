package com.amazon.service_b.payment.infrastructure.gateway;

import com.amazon.service_b.payment.domain.PaymentGateway;
import com.amazon.service_b.payment.domain.exception.InsufficientFundsException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SimulatedPaymentGateway implements PaymentGateway {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000");

    @Override
    public void charge(Money amount) {
        if (amount.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new InsufficientFundsException(amount.amount());
        }
    }
}
