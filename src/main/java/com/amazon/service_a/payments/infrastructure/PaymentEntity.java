package com.amazon.service_a.payments.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.amazon.service_a.payments.domain.Payment;


@Entity
@Table(name = "payments")
@Getter
@Setter
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Payment.State state;
}
