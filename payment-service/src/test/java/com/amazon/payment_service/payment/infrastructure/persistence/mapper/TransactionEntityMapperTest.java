package com.amazon.payment_service.payment.infrastructure.persistence.mapper;

import com.amazon.payment_service.payment.domain.Transaction;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.TransactionEntity;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionEntityMapperTest {

    private final TransactionEntityMapper mapper = new TransactionEntityMapper();

    @Test
    void toEntity_mapsIdAndAmount() {
        Transaction transaction = Transaction.create(new Money(new BigDecimal("50.00")));

        TransactionEntity entity = mapper.toEntity(transaction);

        assertThat(entity.getId()).isEqualTo(transaction.id());
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void toDomain_mapsIdAndAmount() {
        UUID id = UUID.randomUUID();
        TransactionEntity entity = new TransactionEntity();
        entity.setId(id);
        entity.setAmount(new BigDecimal("75.00"));

        Transaction transaction = mapper.toDomain(entity);

        assertThat(transaction.id()).isEqualTo(id);
        assertThat(transaction.money()).isEqualTo(new Money(new BigDecimal("75.00")));
    }
}
