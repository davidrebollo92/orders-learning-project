package com.amazon.inventory_service.reservation.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import com.amazon.inventory_service.reservation.aplication.StockConfirmer;
import com.amazon.inventory_service.reservation.aplication.StockReleaser;
import com.amazon.inventory_service.reservation.domain.exception.InvalidReservationStateException;
import com.amazon.inventory_service.reservation.domain.exception.ReservationNotFoundException;
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
public class PaymentKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentKafkaEventConsumer.class);

    private final StockConfirmer stockConfirmer;
    private final StockReleaser stockReleaser;
    private final JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.paymentsCompleted}", groupId = "inventory-stock-updater")
    public void consumeCompleted(PaymentCompletedEvent event) {
        try {
            stockConfirmer.confirm(UUID.fromString(event.getOrderId()));
        } catch (ReservationNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown reservation: {}", ex.getMessage());
        } catch (ProductNotFoundException ex) {
            log.error("PaymentCompletedEvent received for unknown product: {}", ex.getMessage());
        } catch (InvalidReservationStateException ex) {
            log.error("PaymentCompletedEvent received for reservation in invalid state: {}", ex.getMessage());
        }
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.paymentsFailed}", groupId = "inventory-stock-updater")
    public void consumeFailed(PaymentFailedEvent event) {
        try {
            stockReleaser.release(UUID.fromString(event.getOrderId()));
        } catch (ReservationNotFoundException ex) {
            log.error("PaymentFailedEvent received for unknown reservation: {}", ex.getMessage());
        } catch (ProductNotFoundException ex) {
            log.error("PaymentFailedEvent received for unknown product: {}", ex.getMessage());
        } catch (InvalidReservationStateException ex) {
            log.error("PaymentFailedEvent received for reservation in invalid state: {}", ex.getMessage());
        }
    }

    @DltHandler
    public void handleDlt(
            SpecificRecord event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(value = "kafka_dlt-exception-message", required = false) String exceptionMessage) {
        log.error("DLT: Event could not be processed after retries: topic={} partition={} offset={}", topic, partition, offset);
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
