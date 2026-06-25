package com.amazon.payment_service.payment.infrastructure.messaging;

import com.amazon.shared.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class PaymentKafkaTopicConfig {

    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Bean
    public NewTopic paymentsCompletedTopic() {
        return TopicBuilder.name(kafkaTopicsConfig.getPaymentsCompleted()).build();
    }

    @Bean
    public NewTopic paymentsFailedTopic() {
        return TopicBuilder.name(kafkaTopicsConfig.getPaymentsFailed()).build();
    }
}
