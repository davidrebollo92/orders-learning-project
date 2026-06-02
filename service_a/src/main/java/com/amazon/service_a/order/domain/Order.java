package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_boot.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Order(UUID id, String name, Money amount, Payment payment) {

    public Order {
        if (amount.isBelowMinimum()) throw new InvalidOrderAmountException();
    }

    public Order addPayment() {
        final Payment payment = new Payment(UUID.randomUUID(), Payment.State.PENDING);
        return new Order(id, name, amount, payment);
    }

    public static Order create(String name, Money amount) {
        return new Order(UUID.randomUUID(), name, amount, null);
    }

    public Order completePayment() {
        return toBuilder().withPayment(payment.pay()).build();
    }
}
