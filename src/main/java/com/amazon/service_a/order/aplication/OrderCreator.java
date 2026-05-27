package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreator {

    private final OrderRepositoryPort orderRepositoryPort;
    private final OrderEventPublisherPort eventPublisherPort;

    @Transactional
    public Order create(Order order) {
        //TODO esto es una redFlag -> esta operativa tiene que esta en dominio
        // metodo estático Order.create(String, Money) y en dominio te crea el Order con un pago pendiente
        // dicho metodo lo puedes meter en OrderDtoMapper (cambiar a OrderMapper) cuando mapear de CreateOrderRequest a Order
        Order orderWithPayment = new Order(order.id(), order.name(), order.amount(), new Payment(null, Payment.State.CREATED));
        Order created = orderRepositoryPort.create(orderWithPayment);

        eventPublisherPort.publishOrderCreated(created);

        return created;
    }
}
