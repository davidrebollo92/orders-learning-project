package com.amazon.service_a.orders.domain;

import java.util.Optional;

// TODO esto no deberia de estar aqui. Deberia ir a com.amazon.service_a.payments.domain
public interface PaymentRepositoryPort {
    Optional<Payment> findById(Long paymentId);
}
