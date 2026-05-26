package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrderCreator {

    private final OrderRepositoryPort orderRepositoryPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderEventPublisherPort eventPublisherPort;

    public OrderCreator(OrderRepositoryPort orderRepositoryPort, PaymentRepositoryPort paymentRepositoryPort, OrderEventPublisherPort eventPublisherPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Transactional
    public Order create(Order order) {
        Payment payment = paymentRepositoryPort.create(new Payment(null, Payment.State.CREATED));

        Order orderWithPayment = new Order(order.id(), order.name(), order.amount(), payment);
        Order created = orderRepositoryPort.create(orderWithPayment);

        eventPublisherPort.publishOrderCreated(created);

        return created;
    }
}
