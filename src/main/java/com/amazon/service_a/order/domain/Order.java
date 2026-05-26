package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_a.shared.domain.vo.Money;

public record Order(Long id, String name, Money amount, Payment payment) {

    public Order {
        if (amount.isBelowMinimum()) throw new InvalidOrderAmountException();
    }
}


