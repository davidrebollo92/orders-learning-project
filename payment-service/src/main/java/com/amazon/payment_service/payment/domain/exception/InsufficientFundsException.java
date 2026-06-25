package com.amazon.payment_service.payment.domain.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends PaymentDomainException {

    public InsufficientFundsException(BigDecimal amount) {
        super("Insufficient funds for amount: " + amount, "INSUFFICIENT_FUNDS");
    }
}
