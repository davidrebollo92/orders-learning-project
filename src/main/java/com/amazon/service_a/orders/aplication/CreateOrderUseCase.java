package com.amazon.service_a.orders.aplication;

import com.amazon.service_a.orders.domain.Order;

public interface CreateOrderUseCase {
    Order createOrder(Order order);
}
