package com.amazon.service_a.money.domain;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

    private static final BigDecimal MINIMUM = new BigDecimal("0.01");

    public Boolean isBelowMinimum() {
        return this.amount.compareTo(MINIMUM) < 0;
    }
}
