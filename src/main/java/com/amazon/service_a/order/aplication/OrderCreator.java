package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepositoryPort orderRepositoryPort;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderEventPublisherPort eventPublisherPort;

    @Transactional
    public Order create(Order order) {
        Payment payment = paymentRepositoryPort.create(new Payment(null, Payment.State.CREATED));

        Order orderWithPayment = new Order(order.id(), order.name(), order.amount(), payment);
        Order created = orderRepositoryPort.create(orderWithPayment);

        eventPublisherPort.publishOrderCreated(created);

        return created;
    }
}
