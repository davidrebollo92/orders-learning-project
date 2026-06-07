package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepository;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
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
class OrderCancellerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderCanceller orderCanceller;

    @Test
    void cancel_updatesOrderToCancelled_whenOrderAndPaymentMatch() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        orderCanceller.cancel(order.id(), order.payment().id());

        verify(orderRepository).updatePayment(any(Order.class));
    }

    @Test
    void cancel_throwsOrderNotFoundException_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCanceller.cancel(orderId, UUID.randomUUID()))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void cancel_throwsPaymentNotFoundException_whenPaymentIdDoesNotMatch() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00"))).addPayment();
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderCanceller.cancel(order.id(), UUID.randomUUID()))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
