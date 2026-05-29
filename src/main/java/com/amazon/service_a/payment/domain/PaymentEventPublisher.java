package com.amazon.service_a.payment.domain;

public interface PaymentEventPublisher {
    void publishPaymentCompleted(Payment payment);
}
