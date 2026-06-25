package com.amazon.payment_service.payment.domain;


public interface PaymentGateway {
    Payment process(Payment payment);
}
