package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.domain.exception.InvalidPaymentStateException;
import com.amazon.service_b.payment.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.service_b.payment.infrastructure.persistence.entity.OutboxEventEntity;
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
public class PaymentOutboxEventPublisher implements PaymentEventPublisher {

    private final JpaOutboxEventRepository jpaOutboxEventRepository;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publish(Payment payment) {
        switch (payment.state()){
            case PAID: publishPaymentCompleted(payment); break;
            case FAILED: publishPaymentFailed(payment); break;
            default: throw new InvalidPaymentStateException();
        }
    }

    private void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = PaymentCompletedEvent.newBuilder()
                .setPaymentId(payment.id().toString())
                .setOrderId(payment.orderId().toString())
                .build();

        jpaOutboxEventRepository.save(new OutboxEventEntity(
                UUID.randomUUID(),
                payment.id(),
                kafkaTopicsConfig.getPaymentsCompleted(),
                toJson(event),
                PaymentCompletedEvent.class.getName(),
                Instant.now(),
                null
        ));
    }

    private void publishPaymentFailed(Payment payment) {
        PaymentFailedEvent event = PaymentFailedEvent.newBuilder()
                .setPaymentId(payment.id().toString())
                .setOrderId(payment.orderId().toString())
                .build();

        jpaOutboxEventRepository.save(new OutboxEventEntity(
                UUID.randomUUID(),
                payment.id(),
                kafkaTopicsConfig.getPaymentsFailed(),
                toJson(event),
                PaymentFailedEvent.class.getName(),
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
