package com.amazon.payment_service.payment.infrastructure.persistence.entity;

import com.amazon.payment_service.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentEntity {
    @Id
    private UUID id;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    private Payment.State state;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
}
