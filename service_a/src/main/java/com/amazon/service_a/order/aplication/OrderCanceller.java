package com.amazon.service_a.order.aplication;

import com.amazon.service_a.order.domain.Order;
import com.amazon.service_a.order.domain.OrderRepository;
import com.amazon.service_a.order.domain.exception.OrderNotFoundException;
import com.amazon.service_a.order.domain.exception.PaymentNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCanceller {

    private final OrderRepository orderRepository;

    @Transactional
    public void cancel(UUID orderId, UUID paymentId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);

        if (optionalOrder.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }

        if (!optionalOrder.get().payment().id().equals(paymentId)) {
            throw new PaymentNotFoundException(paymentId);
        }

        Order cancelledOrder = optionalOrder.get().cancel();
        orderRepository.updatePayment(cancelledOrder);
    }
}
