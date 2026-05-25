package com.amazon.service_a.orders.domain;

import java.util.Optional;

// TODO Este puerto es innecesario
public interface PaymentRepositoryPort {
    Optional<Payment> findById(Long paymentId);
}
