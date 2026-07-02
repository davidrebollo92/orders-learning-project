package com.amazon.inventory_service.reservation.aplication;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.domain.ProductRepository;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.domain.ReservationRepository;
import com.amazon.inventory_service.reservation.domain.exception.ReservationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReleaser {
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void release(UUID orderId) {
        Reservation reservation = reservationRepository.findByOrderId(orderId).orElseThrow(() -> new ReservationNotFoundException(orderId));
        Product product = productRepository.findByIdForUpdate(reservation.productId()).orElseThrow(() -> new ProductNotFoundException(reservation.productId()));

        final Product productReleased = product.releaseReservation(reservation.quantity());
        final Reservation reservationReleased = reservation.release();

        productRepository.updateStock(productReleased);
        reservationRepository.updateState(reservationReleased);
    }
}
