package com.amazon.service_a.payment.infrastructure.persistence;

import com.amazon.service_a.payment.domain.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "ProcessedPaymentEntity")
@Table(name = "processed_payments")
@Getter
@Setter
public class PaymentEntity {
    @Id
    private UUID id;

    private Payment.State state;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
}
