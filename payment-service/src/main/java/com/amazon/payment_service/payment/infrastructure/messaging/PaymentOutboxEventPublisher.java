package com.amazon.payment_service.payment.infrastructure.messaging;

import com.amazon.avro.PaymentCompletedEvent;
import com.amazon.avro.PaymentFailedEvent;
import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentEventPublisher;
import com.amazon.payment_service.payment.domain.exception.InvalidPaymentStateException;
import com.amazon.payment_service.payment.infrastructure.persistence.JpaOutboxEventRepository;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.OutboxEventEntity;
import com.amazon.shared.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
                toAvroBytes(event),
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
                toAvroBytes(event),
                PaymentFailedEvent.class.getName(),
                Instant.now(),
                null
        ));
    }

    private byte[] toAvroBytes(SpecificRecord record) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DatumWriter<SpecificRecord> writer = new SpecificDatumWriter<>(record.getSchema());

            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);

            writer.write(record, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize Avro record", e);
        }
    }
}
