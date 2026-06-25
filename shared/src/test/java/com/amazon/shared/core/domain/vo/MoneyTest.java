package com.amazon.shared.core.domain.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void isBelowMinimum_returnsFalse_whenAmountEqualsMinimum() {
        assertThat(new Money(new BigDecimal("0.01")).isBelowMinimum()).isFalse();
    }

    @Test
    void isBelowMinimum_returnsFalse_whenAmountIsAboveMinimum() {
        assertThat(new Money(new BigDecimal("100.00")).isBelowMinimum()).isFalse();
    }

    @Test
    void isBelowMinimum_returnsTrue_whenAmountIsZero() {
        assertThat(new Money(BigDecimal.ZERO).isBelowMinimum()).isTrue();
    }

    @Test
    void isBelowMinimum_returnsTrue_whenAmountIsNegative() {
        assertThat(new Money(new BigDecimal("-1.00")).isBelowMinimum()).isTrue();
    }

    @Test
    void isBelowMinimum_returnsTrue_whenAmountIsBelowMinimum() {
        assertThat(new Money(new BigDecimal("0.009")).isBelowMinimum()).isTrue();
    }
}
