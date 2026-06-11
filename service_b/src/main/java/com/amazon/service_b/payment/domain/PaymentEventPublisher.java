package com.amazon.service_b.payment.domain;

public interface PaymentEventPublisher {
    void publish(Payment payment);
}
