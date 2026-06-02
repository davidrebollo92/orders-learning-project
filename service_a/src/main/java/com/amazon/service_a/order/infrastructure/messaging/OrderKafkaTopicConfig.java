package com.amazon.service_a.order.infrastructure.messaging;

import com.amazon.service_boot.core.infrastructure.messaging.KafkaTopicsConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class OrderKafkaTopicConfig {

    private final KafkaTopicsConfig kafkaTopicsConfig;

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name(kafkaTopicsConfig.getOrders()).build();
    }
}
