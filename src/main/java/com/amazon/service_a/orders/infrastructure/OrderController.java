package com.amazon.service_a.orders.infrastructure;

import com.amazon.service_a.orders.aplication.CreateOrderUseCase;
import com.amazon.service_a.orders.aplication.GetAllOrdersUseCase;
import com.amazon.service_a.orders.aplication.GetOrderUseCase;
import com.amazon.service_a.orders.infrastructure.dto.CreateOrderRequest;
import com.amazon.service_a.orders.infrastructure.dto.OrderDtoMapper;
import com.amazon.service_a.orders.infrastructure.dto.OrderListResponse;
import com.amazon.service_a.orders.infrastructure.dto.OrderResponse;
import com.amazon.service_a.shared.infrastructure.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createUseCase;
    private final GetOrderUseCase getUseCase;
    private final GetAllOrdersUseCase getAllUseCase;

    public OrderController(CreateOrderUseCase createUseCase, GetAllOrdersUseCase getAllUseCase, GetOrderUseCase getUseCase) {
        this.createUseCase = createUseCase;
        this.getUseCase = getUseCase;
        this.getAllUseCase = getAllUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = OrderDtoMapper.toResponse(
                createUseCase.createOrder(OrderDtoMapper.toDomain(request))
        );
        
        return ApiResponse.ok("Order created successfully", order);
    }

    @GetMapping
    public ApiResponse<OrderListResponse> getAll() {
        List<OrderResponse> orders = getAllUseCase.getAllOrders().stream()
                .map(OrderDtoMapper::toResponse)
                .toList();

        return ApiResponse.ok("Orders retrieved successfully", new OrderListResponse(orders.size(), orders));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        OrderResponse order = OrderDtoMapper.toResponse(getUseCase.getOrder(id));

        return ApiResponse.ok("Order retrieved successfully", order);
    }
}