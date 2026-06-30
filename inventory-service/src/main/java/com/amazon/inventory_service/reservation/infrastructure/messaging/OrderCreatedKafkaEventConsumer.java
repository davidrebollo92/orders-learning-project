package com.amazon.inventory_service.reservation.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.inventory_service.product.domain.exception.InsufficientStockException;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import com.amazon.inventory_service.reservation.aplication.StockReserver;
import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.domain.exception.ReservationAlreadyExistsException;
import com.amazon.inventory_service.reservation.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.inventory_service.reservation.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.amazon.shared.core.infrastructure.messaging.AvroUtils;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCreatedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaEventConsumer.class);

    private final StockReserver stockReserver;
    private final JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.ordersCreated}", groupId = "inventory-stock-reserver")
    public void consume(OrderCreatedEvent event) {
        try{
            stockReserver.reserve(Reservation.create(
                    UUID.fromString(event.getOrderId()),
                    UUID.fromString(event.getProductId()),
                    event.getQuantity()
            ));
        } catch (ReservationAlreadyExistsException ex) {
            log.warn("Duplicate OrderCreatedEvent received: {}", ex.getMessage());
        } catch (ProductNotFoundException ex) {
            log.error("OrderCreatedEvent received for unknown product: {}", ex.getMessage());
        } catch (InsufficientStockException ex) {
            log.warn("Insufficient stock for order: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(
            OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "kafka_dlt-exception-message", required = false) String exceptionMessage) {
        log.error("DLT: OrderCreatedEvent could not be processed after retries: topic={} partition={} offset={}", topic, partition, offset);
        saveDeadLetterEvent(event, topic, exceptionMessage, partition, offset);
    }

    private void saveDeadLetterEvent(SpecificRecord event, String topic, String exceptionMessage, int partition, long offset) {
        try {
            byte[] payload = AvroUtils.toBytes(event);

            jpaDeadLetterEventRepository.save(new DeadLetterEventEntity(
                    UUID.randomUUID(),
                    topic,
                    payload,
                    event.getClass().getName(),
                    exceptionMessage,
                    partition,
                    offset,
                    Instant.now()
            ));
        } catch (Exception e) {
            log.error("Failed to persist DLT event to database: topic={}", topic, e);
        }
    }
}
