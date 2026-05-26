package com.amazon.service_a.order.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, Long> {
}
