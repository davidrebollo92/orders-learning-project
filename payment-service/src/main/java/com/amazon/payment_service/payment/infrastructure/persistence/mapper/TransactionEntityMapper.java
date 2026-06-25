package com.amazon.payment_service.payment.infrastructure.persistence.mapper;

import com.amazon.payment_service.payment.domain.Transaction;
import com.amazon.payment_service.payment.infrastructure.persistence.entity.TransactionEntity;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {
    public Transaction toDomain(TransactionEntity entity) {
        return new Transaction(entity.getId(), new Money(entity.getAmount()));
    }

    public TransactionEntity toEntity(Transaction transaction) {
        TransactionEntity entity = new TransactionEntity();

        entity.setId(transaction.id());
        entity.setAmount(transaction.money().amount());

        return entity;
    }

}
