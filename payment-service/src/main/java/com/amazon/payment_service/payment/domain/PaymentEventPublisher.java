package com.amazon.payment_service.payment.domain;

public interface PaymentEventPublisher {
    void publish(Payment payment);
}
