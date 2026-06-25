package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderRepository;
import com.amazon.order_service.order.domain.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderFinder {

    private final OrderRepository orderRepository;

    public List<Order> findAll() {
        return orderRepository.getAll();
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
