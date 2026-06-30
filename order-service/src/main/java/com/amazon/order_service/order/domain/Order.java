package com.amazon.order_service.order.domain;

import com.amazon.order_service.order.domain.exception.InvalidOrderAmountException;
import com.amazon.shared.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Order(UUID id, UUID productId, int quantity, Money money, State state, Payment payment) {

    public enum State {
        CREATED,
        PAID,
        CANCELLED
    }

    public Order {
        if (money.isBelowMinimum()) throw new InvalidOrderAmountException();
    }

    public static Order create(UUID productId, int quantity, Money money) {
        return new Order(UUID.randomUUID(), productId, quantity, money, State.CREATED, null);
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
