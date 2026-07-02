package com.amazon.order_service.order.infrastructure.persistence.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.Payment;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class OrderEntityMapper {

    public OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity();

        entity.setId(order.id());
        entity.setProductId(order.productId());
        entity.setQuantity(order.quantity());
        entity.setAmount(order.money().amount());
        entity.setState(order.state());
        entity.setPaymentId(order.payment().id());
        entity.setPaymentState(order.payment().state());

        return entity;
    }

    public Order toDomain(OrderEntity entity) {

        return new Order(
                entity.getId(),
                entity.getProductId(),
                entity.getQuantity(),
                new Money(entity.getAmount()),
                entity.getState(),
                new Payment(entity.getPaymentId(), entity.getPaymentState())
        );
    }
}
