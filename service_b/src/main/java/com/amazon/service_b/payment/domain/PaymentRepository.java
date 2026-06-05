package com.amazon.service_b.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findById(UUID id);

    Payment save(Payment payment);
}
