package com.amazon.service_a.order.infrastructure.http.mapper;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.service_a.order.infrastructure.http.dto.OrderResponse;
import com.amazon.service_a.order.infrastructure.http.dto.PaymentResponse;
import com.amazon.service_a.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper {

    public Order toDomain(CreateOrderRequest request) {
        return Order.create(request.name(), new Money(request.amount()));
    }

    public OrderResponse toResponse(Order order) {
        PaymentResponse paymentResponse = order.payment() != null
                ? new PaymentResponse(
                order.payment().id(),
                order.payment().state().name())
                : null;

        return new OrderResponse(
                order.id(),
                order.name(),
                order.amount().amount(),
                paymentResponse
        );
    }
}
