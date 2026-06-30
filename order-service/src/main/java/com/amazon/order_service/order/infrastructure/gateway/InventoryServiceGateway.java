package com.amazon.order_service.order.infrastructure.gateway;

import com.amazon.order_service.order.domain.ProductData;
import com.amazon.order_service.order.domain.ProductGateway;
import com.amazon.order_service.order.domain.exception.ProductNotFoundException;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Component
public class InventoryServiceGateway implements ProductGateway {

    private final RestClient restClient;

    public InventoryServiceGateway(@Value("${app.inventory-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public ProductData findById(UUID productId) {
        try {
            ProductResponse response = Objects.requireNonNull(restClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class));

            return new ProductData(response.id(), new Money(response.price()));
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        }
    }

    private record ProductResponse(UUID id, BigDecimal price) {
    }

}
