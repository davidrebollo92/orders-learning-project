package com.amazon.service_a.orders.infrastructure;

import com.amazon.service_a.orders.domain.Order;
import com.amazon.service_a.orders.domain.OrderEventPublisherPort;
import com.amazon.service_a.orders.infrastructure.dto.OrderCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private static final String TOPIC = "orders.created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisherAdapter(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(),
                order.getName(),
                order.getAmount().amount()
        );

        kafkaTemplate.send(TOPIC, String.valueOf(order.getId()), event);
    }
}
