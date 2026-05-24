package com.amazon.service_a.orders.aplication;

import com.amazon.service_a.orders.domain.Order;

import java.util.List;

public interface GetAllOrdersUseCase {
    List<Order> getAllOrders();
}
