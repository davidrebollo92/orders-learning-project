package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    // TODO cambiar el nombre a amazon.env.order-management.orders.pub
    //  empresa-emisora.entorno.bussines-domain.agregado.[publico|comando]
    //  publico -> snapshot del agregado (sirve para informar de un cambio en el micro que lo produce)
    //  command -> para avisar de que se haga algo en destino (ejecutar algun proceso en los consumidores)
    private static final String TOPIC = "orders.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.id(),
                order.amount().amount(),
                order.payment().id()
        );

        kafkaTemplate.send(TOPIC, String.valueOf(order.id()), event);
    }
}
