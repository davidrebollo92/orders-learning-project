package com.amazon.order_service.order.domain;

import com.amazon.shared.core.domain.vo.Money;

import java.util.UUID;

public record ProductData(UUID id, Money price) {
}