package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.persistence.entity.OrderPaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentEntityMapper {

    public Payment toDomain(OrderPaymentEntity entity) {
        return new Payment(entity.getId(), entity.getState());
    }

    public OrderPaymentEntity toEntity(Payment payment) {
        OrderPaymentEntity entity = new OrderPaymentEntity();

        entity.setId(payment.id());
        entity.setState(payment.state());

        return entity;
    }
}
