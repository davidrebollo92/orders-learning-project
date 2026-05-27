package com.amazon.service_a.order.infrastructure.persistence;

import com.amazon.service_a.order.domain.Payment;
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

    private Payment.State state;
}
