package com.amazon.service_a.payment.infrastructure.messaging;

import com.amazon.service_a.payment.domain.Payment;
import com.amazon.service_a.payment.domain.PaymentEventPublisher;
import com.amazon.service_a.shared.infrastructure.messaging.KafkaTopicsConfig;
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
        PaymentCompletedEvent event = new PaymentCompletedEvent(payment.id());

        kafkaTemplate.send(kafkaTopicsConfig.getPayments(), payment.id().toString(), event);
    }
}
