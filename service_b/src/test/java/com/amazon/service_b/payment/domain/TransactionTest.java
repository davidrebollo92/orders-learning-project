package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void create_returnsTransactionWithNullIdAndAmount() {
        Money money = new Money(new BigDecimal("100.00"));

        Transaction transaction = Transaction.create(money);

        assertThat(transaction.id()).isNull();
        assertThat(transaction.money()).isEqualTo(money);
    }

    @Test
    void complete_returnsTransactionWithAssignedId() {
        Money money = new Money(new BigDecimal("100.00"));
        UUID transactionId = UUID.randomUUID();

        Transaction transaction = Transaction.create(money).complete(transactionId);

        assertThat(transaction.id()).isEqualTo(transactionId);
        assertThat(transaction.money()).isEqualTo(money);
    }
}
