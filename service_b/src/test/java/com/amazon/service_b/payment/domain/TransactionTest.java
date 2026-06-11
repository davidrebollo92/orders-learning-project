package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void create_returnsTransactionWithNullIdAndAmount() {
        Money amount = new Money(new BigDecimal("100.00"));

        Transaction transaction = Transaction.create(amount);

        assertThat(transaction.id()).isNull();
        assertThat(transaction.amount()).isEqualTo(amount);
    }

    @Test
    void complete_returnsTransactionWithAssignedId() {
        Money amount = new Money(new BigDecimal("100.00"));
        UUID transactionId = UUID.randomUUID();

        Transaction transaction = Transaction.create(amount).complete(transactionId);

        assertThat(transaction.id()).isEqualTo(transactionId);
        assertThat(transaction.amount()).isEqualTo(amount);
    }
}
