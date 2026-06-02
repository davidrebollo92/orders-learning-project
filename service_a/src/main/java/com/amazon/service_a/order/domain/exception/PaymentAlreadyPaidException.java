package com.amazon.service_a.order.domain.exception;

import java.util.UUID;

public class PaymentAlreadyPaidException extends OrderDomainException {

    public PaymentAlreadyPaidException(UUID id) {
        super("Payment already paid with id: " + id, "PAYMENT_ALREADY_PAID");
    }
}
