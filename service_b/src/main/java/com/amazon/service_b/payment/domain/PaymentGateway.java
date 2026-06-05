package com.amazon.service_b.payment.domain;

import com.amazon.service_boot.core.domain.vo.Money;

public interface PaymentGateway {
    void charge(Money amount);
}
