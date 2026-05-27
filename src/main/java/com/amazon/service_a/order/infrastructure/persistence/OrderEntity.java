package com.amazon.service_a.order.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {

    @Id
    private UUID id;

    private String name;

    private BigDecimal amount;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;
}
