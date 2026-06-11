package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Transaction(UUID id, Money money) {

    public Transaction complete(UUID id) {
        return toBuilder().withId(id).build();
    }

    public static Transaction create(Money money) {
        return new Transaction(null, money);
    }
}
