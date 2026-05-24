package com.amazon.service_a.orders.domain;

import lombok.Getter;
import lombok.Setter;

import com.amazon.service_a.money.domain.Money;
import com.amazon.service_a.orders.domain.exception.InvalidOrderAmountException;

@Getter
@Setter
public class Order {
    private Long id;
    private String name;
    private Money amount;
    private Payment payment;

    public void setAmount(Money amount) {
        if (amount.isBelowMinimum()) throw new InvalidOrderAmountException();

        this.amount = amount;
    }

}


