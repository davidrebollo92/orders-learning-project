package com.amazon.service_a.payments.infrastructure;

import com.amazon.service_a.payments.domain.Payment;
import com.amazon.service_a.payments.domain.PaymentRepositoryPort;
import com.amazon.service_a.payments.infrastructure.JpaPaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final JpaPaymentRepository jpaPaymentRepository;

    public PaymentRepositoryAdapter(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaPaymentRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }
}
