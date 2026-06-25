package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderEventPublisher;
import com.amazon.order_service.order.domain.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    @Transactional
    public Order create(Order order) {
        Order created = orderRepository.save(order.addPayment());

        orderEventPublisher.publishOrderCreated(created);

        return created;
    }
}
