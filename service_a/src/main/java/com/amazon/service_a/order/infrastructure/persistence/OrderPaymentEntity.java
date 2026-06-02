package com.amazon.service_a.order.infrastructure.persistence;

import com.amazon.service_a.order.domain.Payment;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private Payment.State state;
}
