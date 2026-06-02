package com.amazon.service_b.payment.infrastructure.messaging;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.shared.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventPublisher implements PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Override
    public void publishPaymentCompleted(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(payment.id(), payment.orderId());

        kafkaTemplate.send(kafkaTopicsConfig.getPayments(), payment.id().toString(), event);
    }
}
