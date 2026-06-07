package com.amazon.service_a.order.domain;

import com.amazon.service_a.order.domain.exception.InvalidOrderAmountException;
import com.amazon.service_boot.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Order(UUID id, String name, Money amount, State state, Payment payment) {

    public enum State {
        CREATED,
        PAID,
        CANCELLED
    }

    public Order {
        if (amount.isBelowMinimum()) throw new InvalidOrderAmountException();
    }

    public static Order create(String name, Money amount) {
        return new Order(UUID.randomUUID(), name, amount, State.CREATED, null);
    }

    public Order addPayment() {
        Payment payment = new Payment(UUID.randomUUID(), Payment.State.PENDING);
        return toBuilder().withPayment(payment).build();
    }

    public Order completePayment() {
        return toBuilder().withPayment(payment.pay()).withState(State.PAID).build();
    }

    public Order cancel() {
        return toBuilder().withPayment(payment.fail()).withState(State.CANCELLED).build();
    }
}
