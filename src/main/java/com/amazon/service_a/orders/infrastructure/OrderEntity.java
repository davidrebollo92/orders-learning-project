package com.amazon.service_a.orders.infrastructure;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(name = "orders")
@Getter
@Setter
// TODO moverlo a com.amazon.service_a.orders.infrastructure.persistence
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal amount;

    @Column(name = "payment_id")
    private Long paymentId;
}
