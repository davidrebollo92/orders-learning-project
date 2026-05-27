package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_a.shared.domain.vo.Money;

import java.util.UUID;

public record Order(UUID id, String name, Money amount, Payment payment) {

    public Order {
        if (amount.isBelowMinimum()) throw new InvalidOrderAmountException();
    }

    public static Order create(String name, Money amount) {
        return new Order(UUID.randomUUID(), name, amount, new Payment(UUID.randomUUID(), Payment.State.PENDING));
    }
}


