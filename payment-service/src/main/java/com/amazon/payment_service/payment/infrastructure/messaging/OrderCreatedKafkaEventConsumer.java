package com.amazon.payment_service.payment.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.payment_service.payment.aplication.PaymentCreator;
import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaDeadLetterEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.DeadLetterEventEntity;
import com.amazon.shared.core.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
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

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderCreatedKafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedKafkaEventConsumer.class);

    private final PaymentCreator paymentCreator;
    private final JpaDeadLetterEventRepository jpaDeadLetterEventRepository;

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "#{@kafkaTopicsConfig.ordersCreated}", groupId = "payment-processor")
    public void consume(OrderCreatedEvent event) {
        try {
            Payment payment = Payment.create(UUID.fromString(event.getPaymentId()), UUID.fromString(event.getOrderId()), new Money(new BigDecimal(event.getAmount())));

            paymentCreator.create(payment);
        } catch (PaymentAlreadyPaidException ex) {
            log.warn("Duplicate OrderCreatedEvent received: {}", ex.getMessage());
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
            byte[] payload = toAvroBytes(event);

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

    private byte[] toAvroBytes(SpecificRecord event) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        var encoder = EncoderFactory.get().binaryEncoder(baos, null);

        new SpecificDatumWriter<>(event.getSchema()).write(event, encoder);
        encoder.flush();

        return baos.toByteArray();
    }
}
