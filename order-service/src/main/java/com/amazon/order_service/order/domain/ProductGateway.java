package com.amazon.order_service.order.domain;

import java.util.UUID;

public interface ProductGateway {
    ProductData findById(UUID productId);
}