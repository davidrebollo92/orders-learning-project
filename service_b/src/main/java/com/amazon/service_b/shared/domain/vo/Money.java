package com.amazon.service_b.shared.domain.vo;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

    private static final BigDecimal MINIMUM = new BigDecimal("0.01");

    public boolean isBelowMinimum() {
        return this.amount.compareTo(MINIMUM) < 0;
    }
}
