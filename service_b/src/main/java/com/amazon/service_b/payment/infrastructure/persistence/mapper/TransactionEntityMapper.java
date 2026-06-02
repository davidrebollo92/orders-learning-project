package com.amazon.service_b.payment.infrastructure.persistence.mapper;

import com.amazon.service_b.payment.domain.Transaction;
import com.amazon.service_b.payment.infrastructure.persistence.TransactionEntity;
import com.amazon.service_b.shared.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {
    public Transaction toDomain(TransactionEntity entity) {
        return new Transaction(entity.getId(), new Money(entity.getAmount()));
    }

    public TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();

        entity.setId(transaction.id());
        entity.setAmount(transaction.amount().amount());

        return entity;
    }

}
