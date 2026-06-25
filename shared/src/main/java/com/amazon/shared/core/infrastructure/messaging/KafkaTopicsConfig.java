package com.amazon.shared.core.infrastructure.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kafka.topics")
@Getter
@Setter
public class KafkaTopicsConfig {
    private String ordersCreated;
    private String paymentsCompleted;
    private String paymentsFailed;
}
