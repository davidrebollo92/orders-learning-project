package com.amazon.payment_service.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findById(UUID id);

    Payment save(Payment payment);
}
