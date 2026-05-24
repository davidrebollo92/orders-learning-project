package com.amazon.service_a.orders.infrastructure.dto;

import com.amazon.service_a.money.domain.Money;
import com.amazon.service_a.orders.domain.Order;

public class OrderDtoMapper {

    public static Order toDomain(CreateOrderRequest request) {
        Order order = new Order();
        order.setName(request.name());
        order.setAmount(new Money(request.amount()));
        return order;
    }

    public static OrderResponse toResponse(Order order) {
        PaymentResponse paymentResponse = order.getPayment() != null
                ? new PaymentResponse(
                order.getPayment().id(),
                order.getPayment().state())
                : null;

        return new OrderResponse(
                order.getId(),
                order.getName(),
                order.getAmount().amount(),
                paymentResponse
        );
    }
}
