package com.amazon.inventory_service.product.domain.exception;

import com.amazon.shared.core.domain.exception.DomainException;

public abstract class ProductDomainException extends DomainException {
    protected ProductDomainException(String message, String code) { super(message, code); }
}


