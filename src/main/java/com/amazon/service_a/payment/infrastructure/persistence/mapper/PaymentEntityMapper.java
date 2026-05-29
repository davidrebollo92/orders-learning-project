package com.amazon.service_a.payment.infrastructure.persistence.mapper;

import com.amazon.service_a.payment.domain.Payment;
import com.amazon.service_a.payment.infrastructure.persistence.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("paymentPaymentEntityMapper")
@RequiredArgsConstructor
public class PaymentEntityMapper {
    private final TransactionEntityMapper transactionEntityMapper;

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setId(payment.id());
        entity.setState(payment.state());
        entity.setTransaction(payment.transaction() != null ? transactionEntityMapper.toEntity(payment.transaction()) : null);

        return entity;
    }

    public Payment toDomain(PaymentEntity paymentEntity) {
        return new Payment(
                paymentEntity.getId(),
                paymentEntity.getState(),
                paymentEntity.getTransaction() != null ? transactionEntityMapper.toDomain(paymentEntity.getTransaction()) : null
        );
    }
}
