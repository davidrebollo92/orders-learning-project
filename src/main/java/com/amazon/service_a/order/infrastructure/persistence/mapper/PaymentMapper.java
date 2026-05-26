package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Payment;
import com.amazon.service_a.order.infrastructure.persistence.PaymentEntity;

public class PaymentMapper {

    public static Payment toDomain(PaymentEntity entity) {
        return new Payment(entity.getId(), entity.getState());
    }
}
