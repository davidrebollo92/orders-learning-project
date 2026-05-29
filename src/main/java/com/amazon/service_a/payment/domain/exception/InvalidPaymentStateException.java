package com.amazon.service_a.payment.domain.exception;

public class InvalidPaymentStateException extends PaymentDomainException {

    public InvalidPaymentStateException() {
        super("A paid payment must have a transaction", "INVALID_PAYMENT_STATE");
    }
}
