package com.amazon.inventory_service.reservation.infrastructure.gateway;

import com.amazon.inventory_service.reservation.domain.OrderGateway;
import com.amazon.inventory_service.reservation.domain.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderServiceGateway implements OrderGateway {

    private final RestClient restClient;

    public OrderServiceGateway(
            @Value("${app.order-service.base-url}") String baseUrl,
            @Value("${app.order-service.connect-timeout}") Duration connectTimeout,
            @Value("${app.order-service.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public Optional<OrderStatus> findStatus(UUID orderId) {
        try {
            OrderResponse response = restClient.get()
                    .uri("/orders/{id}", orderId)
                    .retrieve()
                    .body(OrderResponse.class);

            return Optional.of(OrderStatus.valueOf(Objects.requireNonNull(response).state()));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    private record OrderResponse(String state) {
    }
}
