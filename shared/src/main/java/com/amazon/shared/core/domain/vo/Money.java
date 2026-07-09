package com.amazon.shared.core.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {

    private static final BigDecimal MINIMUM = new BigDecimal("0.01");

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");

        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isBelowMinimum() {
        return this.amount.compareTo(MINIMUM) < 0;
    }
}
