package com.amazon.service_b.payment.infrastructure.persistence.mapper;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.infrastructure.persistence.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEntityMapper {
    private final TransactionEntityMapper transactionEntityMapper;

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setId(payment.id());
        entity.setOrderId(payment.orderId());
        entity.setState(payment.state());
        entity.setTransaction(payment.transaction() != null && payment.transaction().id() != null
                ? transactionEntityMapper.toEntity(payment.transaction())
                : null);

        return entity;
    }

    public Payment toDomain(PaymentEntity paymentEntity) {
        return new Payment(
                paymentEntity.getId(),
                paymentEntity.getOrderId(),
                paymentEntity.getState(),
                paymentEntity.getTransaction() != null ? transactionEntityMapper.toDomain(paymentEntity.getTransaction()) : null
        );
    }
}
