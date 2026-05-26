package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepositoryPort;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFinder {

    private final OrderRepositoryPort orderRepositoryPort;

    public List<Order> findAll() {
        return orderRepositoryPort.getAll();
    }

    public Order findById(Long id) {
        return orderRepositoryPort.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}
