package com.amazon.order_service.order.infrastructure.http.mapper;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.order_service.order.infrastructure.http.dto.PaymentResponse;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class OrderDtoMapper {

    public Order toDomain(CreateOrderRequest request) {
        return Order.create(request.getProductId(), request.getQuantity(), new Money(request.getAmount()));
    }

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
