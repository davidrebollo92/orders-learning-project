package com.amazon.service_a.orders.domain;

import java.util.Optional;

public interface PaymentRepositoryPort {
    Optional<Payment> findById(Long paymentId);
}
