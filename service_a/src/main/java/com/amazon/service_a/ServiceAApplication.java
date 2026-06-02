package com.amazon.service_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"com.amazon.service_a", "com.amazon.service_boot"})
@ConfigurationPropertiesScan({"com.amazon.service_a", "com.amazon.service_boot"})
public class ServiceAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceAApplication.class, args);
    }

}
