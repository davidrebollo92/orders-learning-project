package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.avro.OrderCreatedEvent;
import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderEventPublisher;
import com.amazon.service_a.order.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.service_a.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.amazon.service_boot.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderOutboxEventPublisher implements OrderEventPublisher {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.newBuilder()
                .setOrderId(order.id().toString())
                .setAmount(order.amount().amount().toPlainString())
                .setPaymentId(order.payment().id().toString())
                .build();

        jpaOutboxEventRepository.save(new OutboxEventEntity(
                UUID.randomUUID(),
                order.id(),
                kafkaTopicsConfig.getOrdersCreated(),
                toJson(event),
                OrderCreatedEvent.class.getName(),
                Instant.now(),
                null
        ));
    }

    private String toJson(SpecificRecord record) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());

            JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), out);

            writer.write(record, encoder);
            encoder.flush();

            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Avro record", e);
        }
    }
}
