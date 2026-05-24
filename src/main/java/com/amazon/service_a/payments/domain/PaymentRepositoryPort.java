package com.amazon.service_a.payments.domain;

import java.util.Optional;

public interface PaymentRepositoryPort {
    Optional<Payment> findById(Long id);
}
