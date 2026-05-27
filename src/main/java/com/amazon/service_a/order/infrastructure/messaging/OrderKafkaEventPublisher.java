package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderKafkaEventPublisher implements OrderEventPublisher {

    private static final String TOPIC = "amazon.env.order-management.orders.pub";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.id(),
                order.amount().amount(),
                order.payment().id()
        );

        kafkaTemplate.send(TOPIC, order.id().toString(), event);
    }
}
