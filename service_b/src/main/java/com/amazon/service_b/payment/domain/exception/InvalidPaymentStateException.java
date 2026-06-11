package com.amazon.service_b.payment.domain.exception;

public class InvalidPaymentStateException extends PaymentDomainException {

    public InvalidPaymentStateException() {
        super("Invalid payment state", "INVALID_PAYMENT_STATE");
    }
}
