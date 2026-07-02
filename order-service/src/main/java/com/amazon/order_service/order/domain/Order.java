package com.amazon.order_service.order.domain;

import com.amazon.order_service.order.domain.exception.InvalidOrderAmountException;
import com.amazon.order_service.order.domain.exception.PaymentAlreadyPaidException;
import com.amazon.shared.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.math.BigDecimal;
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

    public static Order create(UUID productId, int quantity, Money price) {
        Money money = new Money(price.amount().multiply(BigDecimal.valueOf(quantity)));
        return new Order(UUID.randomUUID(), productId, quantity, money, State.CREATED, Payment.pending());
    }

    public Order markPaid(UUID paymentId) {
        if (state == State.PAID) {
            throw new PaymentAlreadyPaidException(paymentId);
        }

        return toBuilder().withState(State.PAID).withPayment(Payment.paid(paymentId)).build();
    }

    public Order cancel(UUID paymentId) {
        if (state == State.CANCELLED) {
            return this;
        }

        return toBuilder().withState(State.CANCELLED).withPayment(Payment.failed(paymentId)).build();
    }
}
