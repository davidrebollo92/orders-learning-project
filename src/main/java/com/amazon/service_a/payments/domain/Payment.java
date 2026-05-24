package com.amazon.service_a.payments.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {
    public enum State {
        CREATED,
        PAID
    }

    private Long id;
    private State state;
}
