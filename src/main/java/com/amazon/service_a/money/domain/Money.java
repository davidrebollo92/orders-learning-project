package com.amazon.service_a.money.domain;

import java.math.BigDecimal;

// TODO Los value objects no pueden ir a este nivel. Si es un ValueObject compartido tendría que estar en shared.domain.vo.MyValueObject.
public record Money(BigDecimal amount) {

    private static final BigDecimal MINIMUM = new BigDecimal("0.01");

    public Boolean isBelowMinimum() {
        return this.amount.compareTo(MINIMUM) < 0;
    }
}
