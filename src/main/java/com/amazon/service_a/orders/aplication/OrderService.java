package com.amazon.service_a.orders.aplication;

import org.springframework.stereotype.Service;

import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.OrderRepositoryPort;
import com.amazon.service_a.orders.domain.PaymentRepositoryPort;
import com.amazon.service_a.orders.domain.exception.OrderNotFoundException;

import java.util.List;


@Service
public class OrderService implements CreateOrderUseCase, GetAllOrdersUseCase, GetOrderUseCase {

    private final OrderRepositoryPort repository;
    private final PaymentRepositoryPort paymentRepositoryPort;

    public OrderService(OrderRepositoryPort repository, PaymentRepositoryPort paymentRepositoryPort) {
        this.repository = repository;
        this.paymentRepositoryPort = paymentRepositoryPort;
    }

    @Override
    public Order createOrder(Order order) {
        return repository.create(order);
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
