package com.amazon.order_service.order.infrastructure.http;

import com.amazon.order_service.order.aplication.OrderCreator;
import com.amazon.order_service.order.aplication.OrderFinder;
import com.amazon.order_service.order.infrastructure.http.api.OrdersApi;
import com.amazon.order_service.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.order_service.order.infrastructure.http.dto.OrderResponse;
import com.amazon.order_service.order.infrastructure.http.mapper.OrderDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final OrderCreator orderCreator;
    private final OrderFinder orderFinder;
    private final OrderDtoMapper orderDtoMapper;

    @Override
    public ResponseEntity<OrderResponse> createOrder(CreateOrderRequest createOrderRequest) {
        OrderResponse orderResponse = orderDtoMapper.toResponse(
                orderCreator.create(createOrderRequest.getProductId(), createOrderRequest.getQuantity())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<OrderResponse> ordersResponse = orderFinder.findAll().stream()
                .map(orderDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ordersResponse);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID id) {
        OrderResponse orderResponse = orderDtoMapper.toResponse(orderFinder.findById(id));

        return ResponseEntity.ok(orderResponse);
    }
}
