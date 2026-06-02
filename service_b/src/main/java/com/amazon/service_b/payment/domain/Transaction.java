package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;

import java.util.UUID;

public record Transaction(UUID id, Money amount) {

    public static Transaction create(Money amount) {
        return new Transaction(UUID.randomUUID(), amount);
    }
}
