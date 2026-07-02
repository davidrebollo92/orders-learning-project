package com.amazon.inventory_service.reservation.infrastructure.scheduling;

import com.amazon.inventory_service.reservation.aplication.StockConfirmer;
import com.amazon.inventory_service.reservation.aplication.StockReleaser;
import com.amazon.inventory_service.reservation.domain.OrderGateway;
import com.amazon.inventory_service.reservation.domain.OrderStatus;
import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.domain.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class StaleReservationReaper {

    private static final Logger log = LoggerFactory.getLogger(StaleReservationReaper.class);

    private final ReservationRepository reservationRepository;
    private final OrderGateway orderGateway;
    private final StockReleaser stockReleaser;
    private final StockConfirmer stockConfirmer;
    private final Duration ttl;

    public StaleReservationReaper(
            ReservationRepository reservationRepository,
            OrderGateway orderGateway,
            StockReleaser stockReleaser,
            StockConfirmer stockConfirmer,
            @Value("${app.reservation-reaper.ttl}") Duration ttl) {
        this.reservationRepository = reservationRepository;
        this.orderGateway = orderGateway;
        this.stockReleaser = stockReleaser;
        this.stockConfirmer = stockConfirmer;
        this.ttl = ttl;
    }

    @Scheduled(fixedDelayString = "${app.reservation-reaper.interval}")
    public void reap() {
        Instant cutoff = Instant.now().minus(ttl);

        List<Reservation> staleReservations = reservationRepository.findStalePending(cutoff);

        for (Reservation reservation : staleReservations) {
            reconcile(reservation);
        }
    }

    private void reconcile(Reservation reservation) {
        try {
            Optional<OrderStatus> status = orderGateway.findStatus(reservation.orderId());

            if (status.isEmpty() || status.get() == OrderStatus.CANCELLED) {
                stockReleaser.release(reservation.orderId());
                log.info("Released stale reservation for orderId={} (order status={})", reservation.orderId(), status.orElse(null));
            } else if (status.get() == OrderStatus.PAID) {
                stockConfirmer.confirm(reservation.orderId());
                log.info("Confirmed stale reservation for orderId={} (order status=PAID)", reservation.orderId());
            }
        } catch (Exception e) {
            log.error("Failed to reconcile stale reservation for orderId={}", reservation.orderId(), e);
        }
    }
}
