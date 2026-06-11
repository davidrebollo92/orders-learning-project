package com.amazon.service_b.payment.aplication;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.domain.PaymentGateway;
import com.amazon.service_b.payment.domain.PaymentRepository;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCreator {

    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void create(Payment payment) {
        if (paymentRepository.findById(payment.id()).isPresent()) {
            throw new PaymentAlreadyPaidException(payment.id());
        }

        final Payment paymentProcessed = paymentGateway.process(payment);

        paymentRepository.save(paymentProcessed);
        paymentEventPublisher.publish(paymentProcessed);
    }
}
