package com.amazon.service_b.payment.domain;


public interface PaymentGateway {
    Payment process(Payment payment);
}
