package com.amazon.service_a.order.domain.exception;

import java.util.UUID;

public class PaymentAlreadyCompletedException extends OrderDomainException {

    public PaymentAlreadyCompletedException(UUID id) {
        super("Payment already completed with id: " + id, "PAYMENT_ALREADY_COMPLETED");
    }
}
