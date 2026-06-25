package com.amazon.payment_service.payment.infrastructure.persistence;

import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.PaymentEntity;
import com.amazon.payment_service.payment.infrastructure.persistence.mapper.PaymentEntityMapper;
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
