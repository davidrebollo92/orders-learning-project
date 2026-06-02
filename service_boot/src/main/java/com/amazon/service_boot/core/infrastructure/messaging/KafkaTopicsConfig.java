package com.amazon.service_boot.core.infrastructure.messaging;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.kafka.topics")
@Getter
@Setter
public class KafkaTopicsConfig {
    private String orders;
    private String payments;
}
