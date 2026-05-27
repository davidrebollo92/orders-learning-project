package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.persistence.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentEntityMapper {

    public Payment toDomain(PaymentEntity entity) {
        return new Payment(entity.getId(), entity.getState());
    }

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setId(payment.id());
        entity.setState(payment.state());

        return entity;
    }
}
