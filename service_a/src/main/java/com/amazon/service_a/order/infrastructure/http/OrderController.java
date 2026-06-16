package com.amazon.service_a.order.infrastructure.http;

import com.amazon.service_a.order.aplication.OrderCreator;
import com.amazon.service_a.order.aplication.OrderFinder;
import com.amazon.service_a.order.infrastructure.http.api.OrdersApi;
import com.amazon.service_a.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.service_a.order.infrastructure.http.dto.OrderResponse;
import com.amazon.service_a.order.infrastructure.http.mapper.OrderDtoMapper;
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
        OrderResponse order = orderDtoMapper.toResponse(
                orderCreator.create(orderDtoMapper.toDomain(createOrderRequest))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @Override
    public ResponseEntity<List<OrderResponse>> getOrders() {
        List<OrderResponse> orders = orderFinder.findAll().stream()
                .map(orderDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID id) {
        OrderResponse order = orderDtoMapper.toResponse(orderFinder.findById(id));

        return ResponseEntity.ok(order);
    }
}
