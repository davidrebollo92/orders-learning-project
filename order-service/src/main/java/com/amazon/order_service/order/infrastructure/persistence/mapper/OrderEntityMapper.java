package com.amazon.order_service.order.infrastructure.persistence.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.infrastructure.persistence.entity.OrderEntity;
import com.amazon.shared.core.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEntityMapper {

    private final OrderPaymentEntityMapper paymentEntityMapper;

    public OrderEntity toEntity(Order order) {

        OrderEntity entity = new OrderEntity();

        entity.setId(order.id());
        entity.setName(order.name());
        entity.setAmount(order.money().amount());
        entity.setState(order.state());
        entity.setPayment(paymentEntityMapper.toEntity(order.payment()));

        return entity;
    }

    public Order toDomain(OrderEntity entity) {

        return new Order(
                entity.getId(),
                entity.getName(),
                new Money(entity.getAmount()),
                entity.getState(),
                paymentEntityMapper.toDomain(entity.getPayment())
        );
    }
}
