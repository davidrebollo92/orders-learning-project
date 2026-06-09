package com.amazon.service_b.payment.infrastructure.persistence;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentRepository;
import com.amazon.service_b.payment.infrastructure.persistence.entity.PaymentEntity;
import com.amazon.service_b.payment.infrastructure.persistence.mapper.PaymentEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentPostgreSqlRepository implements PaymentRepository {
    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public Optional<Payment> findById(UUID id) {
        return jpaPaymentRepository.findById(id)
                .map(paymentEntityMapper::toDomain);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentEntityMapper.toEntity(payment);
        PaymentEntity saved = jpaPaymentRepository.save(entity);

        return paymentEntityMapper.toDomain(saved);
    }
}
