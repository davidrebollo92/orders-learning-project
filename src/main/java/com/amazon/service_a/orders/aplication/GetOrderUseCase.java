package com.amazon.service_a.orders.aplication;

import com.amazon.service_a.orders.domain.Order;

public interface GetOrderUseCase {
    Order getOrder(Long id);
}