package com.amazon.order_service.order.aplication;

import com.amazon.order_service.order.domain.Order;
import com.amazon.order_service.order.domain.OrderRepository;
import com.amazon.order_service.order.domain.exception.OrderNotFoundException;
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
    void cancel_updatesOrderToCancelled() {
        Order order = Order.create(UUID.randomUUID(), 2, new Money(new BigDecimal("10.00")));
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        orderCanceller.cancel(order.id(), UUID.randomUUID());

        verify(orderRepository).update(any(Order.class));
    }

    @Test
    void cancel_throwsOrderNotFoundException_whenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderCanceller.cancel(orderId, UUID.randomUUID()))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
