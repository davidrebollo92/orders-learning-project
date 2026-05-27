package com.amazon.service_a.shared.domain.vo;

import java.math.BigDecimal;

public record Money(BigDecimal amount) {

    private static final BigDecimal MINIMUM = new BigDecimal("0.01");

    // TODO me gusta esta validación -> Quita la del DTO porque si por aqui no te va a entrar. De esta manera,
    //  controlas la excepción en el dominio ya que es una regla de dominio
    public Boolean isBelowMinimum() {
        return this.amount.compareTo(MINIMUM) < 0;
    }
}
