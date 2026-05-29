package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCompleter {

    private final PaymentRepository paymentRepository;

    public void complete(UUID paymentId) {
        paymentRepository.completePayment(paymentId);
    }
}
