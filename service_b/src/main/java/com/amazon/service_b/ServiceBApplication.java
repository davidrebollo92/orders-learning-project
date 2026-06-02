package com.amazon.service_b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"com.amazon.service_b", "com.amazon.service_boot"})
@ConfigurationPropertiesScan({"com.amazon.service_b", "com.amazon.service_boot"})
public class ServiceBApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBApplication.class, args);
    }

}
