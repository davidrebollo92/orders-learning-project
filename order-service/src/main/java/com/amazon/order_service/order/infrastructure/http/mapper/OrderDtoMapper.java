package com.amazon.order_service.order.infrastructure.http.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.order_service.order.infrastructure.http.dto.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper {

    public OrderResponse toResponse(Order order) {
        PaymentResponse paymentResponse = new PaymentResponse(
                order.payment().id(),
                PaymentResponse.StateEnum.valueOf(order.payment().state().name()));

        return new OrderResponse(
                order.id(),
                order.productId(),
                order.quantity(),
                order.money().amount(),
                OrderResponse.StateEnum.valueOf(order.state().name()),
                paymentResponse
        );
    }
}
