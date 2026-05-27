package com.amazon.service_a.order.domain;

import java.util.UUID;

public record Payment(UUID id, State state) {

    public enum State {
        PENDING,
        PAID
    }
}
