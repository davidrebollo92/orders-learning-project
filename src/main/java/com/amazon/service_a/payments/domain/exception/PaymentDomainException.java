package com.amazon.service_a.payments.domain.exception;

public abstract class PaymentDomainException extends RuntimeException {

    protected PaymentDomainException(String message) {
        super(message);
    }
}
