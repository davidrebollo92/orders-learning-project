package com.amazon.service_a.order.infrastructure.persistence;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.domain.PaymentRepositoryPort;
import com.amazon.service_a.order.infrastructure.persistence.mapper.PaymentMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final JpaPaymentRepository jpaPaymentRepository;

    public PaymentRepositoryAdapter(JpaPaymentRepository jpaPaymentRepository) {
        this.jpaPaymentRepository = jpaPaymentRepository;
    }

    @Override
    public Payment create(Payment payment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setState(payment.state());

        PaymentEntity saved = jpaPaymentRepository.save(entity);

        return PaymentMapper.toDomain(saved);
    }
}
