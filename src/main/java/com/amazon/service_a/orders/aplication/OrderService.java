package com.amazon.service_a.orders.aplication;

import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.OrderEventPublisherPort;
import com.amazon.service_a.orders.domain.OrderRepositoryPort;
import com.amazon.service_a.orders.domain.PaymentRepositoryPort;
import com.amazon.service_a.orders.domain.exception.OrderNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OrderService implements CreateOrderUseCase, GetAllOrdersUseCase, GetOrderUseCase {

    private final OrderRepositoryPort repository;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final OrderEventPublisherPort eventPublisherPort;

    public OrderService(OrderRepositoryPort repository, PaymentRepositoryPort paymentRepositoryPort, OrderEventPublisherPort eventPublisherPort) {
        this.repository = repository;
        this.paymentRepositoryPort = paymentRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public Order createOrder(Order order) {
        Order savedOrder = repository.create(order);
        eventPublisherPort.publishOrderCreated(savedOrder);

        return savedOrder;
    }


    @Override
    public List<Order> getAllOrders() {
        return repository.getAll();
    }

    @Override
    public Order getOrder(Long id) {
        Order order = repository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getPayment() != null) {
            paymentRepositoryPort.findById(order.getPayment().id())
                    .ifPresent(order::setPayment);
        }

        return order;
    }
}
