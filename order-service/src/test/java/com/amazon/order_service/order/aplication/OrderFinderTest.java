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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderFinderTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderFinder orderFinder;

    @Test
    void findAll_returnsAllOrders() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        when(orderRepository.getAll()).thenReturn(List.of(order));

        List<Order> result = orderFinder.findAll();

        assertThat(result).containsExactly(order);
    }

    @Test
    void findById_returnsOrder_whenFound() {
        Order order = Order.create("laptop", new Money(new BigDecimal("10.00")));
        when(orderRepository.findById(order.id())).thenReturn(Optional.of(order));

        Order result = orderFinder.findById(order.id());

        assertThat(result).isEqualTo(order);
    }

    @Test
    void findById_throwsOrderNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderFinder.findById(id))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
