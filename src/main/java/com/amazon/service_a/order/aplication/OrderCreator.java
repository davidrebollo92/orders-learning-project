package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import com.amazon.service_a.order.domain.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public Order create(Order order) {
        Order created = orderRepository.create(order);

        orderEventPublisher.publishOrderCreated(created);

        return created;
    }
}
