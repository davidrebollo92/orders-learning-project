package com.amazon.order_service.order.infrastructure.persistence.entity;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.Payment;
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

    private UUID productId;

    private int quantity;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private Order.State state;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private Payment.State paymentState;
}
