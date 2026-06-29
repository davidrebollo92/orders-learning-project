package com.amazon.inventory_service.reservation.aplication;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.domain.ProductRepository;
import com.amazon.inventory_service.product.domain.exception.ProductNotFoundException;
import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.domain.ReservationRepository;
import com.amazon.inventory_service.reservation.domain.exception.ReservationAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StockReserver {
    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserve(Reservation reservation) {
        if (reservationRepository.findByOrderId(reservation.orderId()).isPresent()) {
            throw new ReservationAlreadyExistsException(reservation.orderId());
        }

        Product product = productRepository.findById(reservation.productId()).orElseThrow(() -> new ProductNotFoundException(reservation.productId()));

        final Product productReserved = product.reserve(reservation.quantity());

        productRepository.updateStock(productReserved);
        reservationRepository.save(reservation);
    }

}
