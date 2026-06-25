package com.amazon.order_service.order.infrastructure.persistence.entity;

import com.amazon.order_service.order.domain.Payment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "OrderPaymentEntity")
@Table(name = "payments")
@Getter
@Setter
public class OrderPaymentEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Payment.State state;
}
