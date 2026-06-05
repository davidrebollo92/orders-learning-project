package com.amazon.service_b.payment.aplication;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.domain.PaymentGateway;
import com.amazon.service_b.payment.domain.PaymentRepository;
import com.amazon.service_b.payment.domain.Transaction;
import com.amazon.service_b.payment.domain.exception.InsufficientFundsException;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void process(Payment payment, Money amount) {
        if (paymentRepository.findById(payment.id()).isPresent()) {
            throw new PaymentAlreadyPaidException(payment.id());
        }

        try {
            paymentGateway.charge(amount);
        } catch (InsufficientFundsException ex) {
            Payment failedPayment = payment.fail();

            paymentRepository.save(failedPayment);
            paymentEventPublisher.publishPaymentFailed(failedPayment);

            return;
        }

        Transaction transaction = Transaction.create(amount);
        Payment completedPayment = payment.pay(transaction);
        Payment savedPayment = paymentRepository.save(completedPayment);

        paymentEventPublisher.publishPaymentCompleted(savedPayment);
    }
}
