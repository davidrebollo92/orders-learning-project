package com.amazon.service_a.payments.infrastructure;

import com.amazon.service_a.payments.domain.Payment;

public class PaymentMapper {

    public static PaymentEntity toEntity(Payment payment) {

        PaymentEntity entity = new PaymentEntity();

        entity.setId(payment.getId());
        entity.setState(payment.getState());

        return entity;
    }

    public static Payment toDomain(PaymentEntity entity) {

        Payment payment = new Payment();

        payment.setId(entity.getId());
        payment.setState(entity.getState());

        return payment;
    }
}
