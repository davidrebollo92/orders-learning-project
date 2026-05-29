package com.amazon.service_a.payment.infrastructure.messaging;

import java.util.UUID;

public record PaymentCompletedEvent(String type, UUID paymentId) {

    public PaymentCompletedEvent(UUID paymentId) {
        this("PAYMENT_COMPLETED", paymentId);
    }
}
