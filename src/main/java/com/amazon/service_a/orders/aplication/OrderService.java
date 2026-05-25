package com.amazon.service_a.orders.aplication;

import org.springframework.stereotype.Service;

import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.OrderRepositoryPort;
import com.amazon.service_a.orders.domain.PaymentRepositoryPort;
import com.amazon.service_a.orders.domain.exception.OrderNotFoundException;

import java.util.List;


@Service
/* TODO el servicio hace muchas cosas e implmenta interfaces que no tiene que ver una con otra
    Llamar a los servicios de la siguiente manera -> OrderCreator, OrderFinder (en este puedes meter el getAll y getById)
    Eliminar las interfaces
*/
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

        // TODO esta logica debería ir en la capa de persistencia. OrderRepositoryPort debería devolver el Order con el Payment ya cargado
        if (order.getPayment() != null) {
            paymentRepositoryPort.findById(order.getPayment().id())
                    .ifPresent(order::setPayment);
        }

        return order;
    }
}
