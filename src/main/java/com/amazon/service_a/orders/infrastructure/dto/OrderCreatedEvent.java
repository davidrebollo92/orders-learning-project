package com.amazon.service_a.orders.infrastructure.dto;

import java.math.BigDecimal;

public record OrderCreatedEvent(Long orderId, String name, BigDecimal amount) {}
