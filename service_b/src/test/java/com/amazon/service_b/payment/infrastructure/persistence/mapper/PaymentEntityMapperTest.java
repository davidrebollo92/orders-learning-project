package com.amazon.service_b.payment.infrastructure.persistence.mapper;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.Transaction;
import com.amazon.service_b.payment.infrastructure.persistence.entity.PaymentEntity;
import com.amazon.service_b.payment.infrastructure.persistence.entity.TransactionEntity;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentEntityMapperTest {

    private final TransactionEntityMapper transactionMapper = new TransactionEntityMapper();
    private final PaymentEntityMapper mapper = new PaymentEntityMapper(transactionMapper);

    @Test
    void toEntity_mapsAllFieldsWithTransaction() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID())
                .pay(Transaction.create(new Money(new BigDecimal("50.00"))));

        PaymentEntity entity = mapper.toEntity(payment);

        assertThat(entity.getId()).isEqualTo(payment.id());
        assertThat(entity.getOrderId()).isEqualTo(payment.orderId());
        assertThat(entity.getState()).isEqualTo(Payment.State.PAID);
        assertThat(entity.getTransaction()).isNotNull();
        assertThat(entity.getTransaction().getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void toEntity_mapsAllFieldsWithoutTransaction() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID());

        PaymentEntity entity = mapper.toEntity(payment);

        assertThat(entity.getId()).isEqualTo(payment.id());
        assertThat(entity.getOrderId()).isEqualTo(payment.orderId());
        assertThat(entity.getState()).isEqualTo(Payment.State.PENDING);
        assertThat(entity.getTransaction()).isNull();
    }

    @Test
    void toDomain_mapsAllFieldsWithTransaction() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transactionId);
        transactionEntity.setAmount(new BigDecimal("50.00"));

        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setOrderId(orderId);
        entity.setState(Payment.State.PAID);
        entity.setTransaction(transactionEntity);

        Payment payment = mapper.toDomain(entity);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.orderId()).isEqualTo(orderId);
        assertThat(payment.state()).isEqualTo(Payment.State.PAID);
        assertThat(payment.transaction()).isNotNull();
        assertThat(payment.transaction().id()).isEqualTo(transactionId);
        assertThat(payment.transaction().amount()).isEqualTo(new Money(new BigDecimal("50.00")));
    }

    @Test
    void toDomain_mapsAllFieldsWithoutTransaction() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentEntity entity = new PaymentEntity();
        entity.setId(id);
        entity.setOrderId(orderId);
        entity.setState(Payment.State.PENDING);
        entity.setTransaction(null);

        Payment payment = mapper.toDomain(entity);

        assertThat(payment.id()).isEqualTo(id);
        assertThat(payment.orderId()).isEqualTo(orderId);
        assertThat(payment.state()).isEqualTo(Payment.State.PENDING);
        assertThat(payment.transaction()).isNull();
    }
}
