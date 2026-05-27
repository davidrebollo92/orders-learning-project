package com.amazon.service_a.order.domain;

public record Payment(Long id, State state) {

    public enum State {
        CREATED, // TODO cambiar a PENDING -> más representativo
        PAID
    }
}
