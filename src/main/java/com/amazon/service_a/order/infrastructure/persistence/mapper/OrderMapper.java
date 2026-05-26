package com.amazon.service_a.order.infrastructure.persistence.mapper;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.infrastructure.persistence.OrderEntity;
import com.amazon.service_a.order.infrastructure.persistence.PaymentEntity;
import com.amazon.service_a.shared.domain.vo.Money;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity();

        entity.setId(order.id());
        entity.setName(order.name());
        entity.setAmount(order.amount().amount());

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(order.payment().id());
        paymentEntity.setState(order.payment().state());
        entity.setPayment(paymentEntity);

        return entity;
    }

    public static Order toDomain(OrderEntity entity) {

        return new Order(
                entity.getId(),
                entity.getName(),
                new Money(entity.getAmount()),
                PaymentMapper.toDomain(entity.getPayment())
        );
    }
}
