package com.amazon.service_a.payment.infrastructure.messaging;

import com.amazon.service_a.shared.infrastructure.messaging.KafkaTopicsConfig;
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
    public NewTopic paymentsTopic() {
        return TopicBuilder.name(kafkaTopicsConfig.getPayments()).build();
    }
}
