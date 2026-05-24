package com.amazon.service_a.orders.infrastructure;

import com.amazon.service_a.money.domain.Money;
import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.Payment;

public class OrderMapper {

    public static OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity();

        entity.setId(order.getId());
        entity.setName(order.getName());
        entity.setAmount(order.getAmount().amount());
        entity.setPaymentId(
                order.getPayment() != null ? order.getPayment().id() : null
        );

        return entity;
    }

    public static Order toDomain(OrderEntity entity) {

        Order order = new Order();

        order.setId(entity.getId());
        order.setName(entity.getName());
        order.setAmount(new Money(entity.getAmount()));
        order.setPayment(
                entity.getPaymentId() != null ? new Payment(entity.getPaymentId(), null) : null
        );

        return order;
    }
}
