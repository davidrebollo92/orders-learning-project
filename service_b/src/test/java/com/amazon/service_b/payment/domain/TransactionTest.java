package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void create_returnsTransactionWithGeneratedIdAndAmount() {
        Money amount = new Money(new BigDecimal("100.00"));

        Transaction transaction = Transaction.create(amount);

        assertThat(transaction.id()).isNotNull();
        assertThat(transaction.amount()).isEqualTo(amount);
    }
}
