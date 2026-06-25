package com.amazon.payment_service.payment.aplication;

import com.amazon.payment_service.payment.domain.Payment;
import com.amazon.payment_service.payment.domain.PaymentEventPublisher;
import com.amazon.payment_service.payment.domain.PaymentGateway;
import com.amazon.payment_service.payment.domain.PaymentRepository;
import com.amazon.payment_service.payment.domain.exception.PaymentAlreadyPaidException;
import com.amazon.shared.core.domain.vo.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCreatorTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentCreator paymentCreator;

    @Test
    void create_savesPaymentAndPublishesEvent_whenPaymentIsNew() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), new Money(new BigDecimal("50.00")));
        Payment paidPayment = payment.pay(UUID.randomUUID());

        when(paymentRepository.findById(payment.id())).thenReturn(Optional.empty());
        when(paymentGateway.process(payment)).thenReturn(paidPayment);
        when(paymentRepository.save(paidPayment)).thenReturn(paidPayment);

        paymentCreator.create(payment);

        verify(paymentRepository).save(paidPayment);
        verify(paymentEventPublisher).publish(paidPayment);
    }

    @Test
    void create_savesFailedPaymentAndPublishesEvent_whenGatewayFails() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), new Money(new BigDecimal("1500.00")));
        Payment failedPayment = payment.fail();

        when(paymentRepository.findById(payment.id())).thenReturn(Optional.empty());
        when(paymentGateway.process(payment)).thenReturn(failedPayment);
        when(paymentRepository.save(failedPayment)).thenReturn(failedPayment);

        paymentCreator.create(payment);

        verify(paymentRepository).save(failedPayment);
        verify(paymentEventPublisher).publish(failedPayment);
    }

    @Test
    void create_throwsPaymentAlreadyPaidException_whenPaymentAlreadyExists() {
        Payment payment = Payment.create(UUID.randomUUID(), UUID.randomUUID(), new Money(new BigDecimal("50.00")));

        when(paymentRepository.findById(payment.id())).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentCreator.create(payment))
                .isInstanceOf(PaymentAlreadyPaidException.class);
    }
}
