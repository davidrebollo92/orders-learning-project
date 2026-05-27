package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepository;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
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
