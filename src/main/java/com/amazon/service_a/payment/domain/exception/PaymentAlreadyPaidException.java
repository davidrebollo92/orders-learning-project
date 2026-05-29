package com.amazon.service_a.payment.domain.exception;

import java.util.UUID;

public class PaymentAlreadyPaidException extends PaymentDomainException {

    public PaymentAlreadyPaidException(UUID id) {
        super("Payment already paid with id: " + id, "PAYMENT_ALREADY_PAID");
    }
}
