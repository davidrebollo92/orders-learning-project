package com.amazon.service_b.payment.aplication;

import com.amazon.service_b.payment.domain.Payment;
import com.amazon.service_b.payment.domain.PaymentEventPublisher;
import com.amazon.service_b.payment.domain.PaymentRepository;
import com.amazon.service_b.payment.domain.Transaction;
import com.amazon.service_b.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.service_boot.core.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentProcessorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentProcessor paymentProcessor;

    @Test
    void process_savesPaymentAndPublishesEvent_whenPaymentIsNew() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID());
        Money amount = new Money(new BigDecimal("50.00"));
        Payment savedPayment = payment.pay(Transaction.create(amount));

        when(paymentRepository.findById(payment.id())).thenReturn(Optional.empty());
        when(paymentRepository.completePayment(any(Payment.class))).thenReturn(savedPayment);

        paymentProcessor.process(payment, amount);

        verify(paymentRepository).completePayment(any(Payment.class));
        verify(paymentEventPublisher).publishPaymentCompleted(savedPayment);
    }

    @Test
    void process_throwsPaymentAlreadyPaidException_whenPaymentAlreadyExists() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID());
        Money amount = new Money(new BigDecimal("50.00"));

        when(paymentRepository.findById(payment.id())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentProcessor.process(payment, amount))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
