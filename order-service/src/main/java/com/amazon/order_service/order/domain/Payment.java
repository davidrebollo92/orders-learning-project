package com.amazon.order_service.order.domain;

import java.util.UUID;

/**
 * Read-model projection of the payment owned by payment-service.
 * order-service does not decide this state; it mirrors what payment events report.
 */
public record Payment(UUID id, State state) {

    public enum State {
        PENDING,
        PAID,
        FAILED
    }

    public static Payment pending() {
        return new Payment(null, State.PENDING);
    }

    public static Payment paid(UUID id) {
        return new Payment(id, State.PAID);
    }

    public static Payment failed(UUID id) {
        return new Payment(id, State.FAILED);
    }
}
