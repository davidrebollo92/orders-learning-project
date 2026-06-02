package com.amazon.service_b.shared.domain.exception;

public abstract class DomainException extends RuntimeException {

    private final String code;

    protected DomainException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
