package com.amazon.service_b.payment.domain.exception;

public class InvalidPaymentStateException extends PaymentDomainException {

    public InvalidPaymentStateException() {
        super("A paid payment must have a transaction", "INVALID_PAYMENT_STATE");
    }
}
