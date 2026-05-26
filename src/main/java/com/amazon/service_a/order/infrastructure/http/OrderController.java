package com.amazon.service_a.order.infrastructure.http;

import com.amazon.service_a.order.aplication.OrderCreator;
import com.amazon.service_a.order.aplication.OrderFinder;
import com.amazon.service_a.order.infrastructure.http.dto.CreateOrderRequest;
import com.amazon.service_a.order.infrastructure.http.dto.OrderResponse;
import com.amazon.service_a.order.infrastructure.http.mapper.OrderDtoMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderCreator orderCreator;
    private final OrderFinder orderFinder;
    private final OrderDtoMapper orderDtoMapper;

    public OrderController(OrderCreator orderCreator, OrderFinder orderFinder, OrderDtoMapper orderDtoMapper) {
        this.orderCreator = orderCreator;
        this.orderFinder = orderFinder;
        this.orderDtoMapper = orderDtoMapper;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderDtoMapper.toResponse(
                orderCreator.create(orderDtoMapper.toDomain(request))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        List<OrderResponse> orders = orderFinder.findAll().stream()
                .map(orderDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
        OrderResponse order = orderDtoMapper.toResponse(orderFinder.findById(id));

        return ResponseEntity.ok(order);
    }
}
