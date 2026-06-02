package com.amazon.service_b.payment.domain;

public interface PaymentEventPublisher {
    void publishPaymentCompleted(Payment payment);
}
