package com.amazon.service_a.orders.infrastructure.dto;

import com.amazon.service_a.money.domain.Money;
import com.amazon.service_a.orders.domain.Order;

// TODO moverlo a com.amazon.service_a.orders.infrastructure.http.mapper
public class OrderDtoMapper {

    //TODO no deben ser estatico
    public static Order toDomain(CreateOrderRequest request) {
        Order order = new Order();
        order.setName(request.name());
        order.setAmount(new Money(request.amount()));
        return order;
    }
    //TODO no deben ser estatico
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
