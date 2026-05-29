package com.amazon.service_a.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Optional<Payment> findById(UUID id);

    Payment completePayment(Payment payment);
}
