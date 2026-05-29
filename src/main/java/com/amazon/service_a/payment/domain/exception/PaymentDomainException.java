package com.amazon.service_a.payment.domain.exception;

import com.amazon.service_a.shared.domain.exception.DomainException;

public abstract class PaymentDomainException extends DomainException {

    protected PaymentDomainException(String message, String code) {
        super(message, code);
    }
}
