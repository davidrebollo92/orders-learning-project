package com.amazon.payment_service.payment.domain.exception;

import java.util.UUID;

public class PaymentAlreadyPaidException extends PaymentDomainException {

    public PaymentAlreadyPaidException(UUID id) {
        super("Payment already paid with id: " + id, "PAYMENT_ALREADY_PAID");
    }
}
