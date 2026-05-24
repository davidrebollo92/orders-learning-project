package com.amazon.service_a.payments.domain.exception;

public class PaymentNotFoundException extends PaymentDomainException {

    public PaymentNotFoundException(Long id) {
        super("Payment not found with id: " + id);
    }
}
