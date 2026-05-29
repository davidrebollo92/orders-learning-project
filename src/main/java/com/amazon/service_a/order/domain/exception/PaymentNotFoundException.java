package com.amazon.service_a.order.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends OrderDomainException {

    public PaymentNotFoundException(UUID id) {
        super("Payment not found with id: " + id, "PAYMENT_NOT_FOUND");
    }
}
