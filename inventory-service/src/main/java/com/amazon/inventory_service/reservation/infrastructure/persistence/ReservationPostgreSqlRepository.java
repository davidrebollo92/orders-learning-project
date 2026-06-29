package com.amazon.inventory_service.reservation.infrastructure.persistence;

import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.domain.ReservationRepository;
import com.amazon.inventory_service.reservation.infrastructure.persistence.entity.ReservationEntity;
import com.amazon.inventory_service.reservation.infrastructure.persistence.mapper.ReservationEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReservationPostgreSqlRepository implements ReservationRepository {
    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationEntityMapper reservationEntityMapper;

    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity entity = reservationEntityMapper.toEntity(reservation);

        ReservationEntity created = jpaReservationRepository.save(entity);

        return reservationEntityMapper.toDomain(created);
    }

    @Override
    public Optional<Reservation> findByOrderId(UUID orderId) {
        return jpaReservationRepository.findByOrderId(orderId)
                .map(reservationEntityMapper::toDomain);
    }

    @Override
    public void updateState(Reservation reservation) {
        jpaReservationRepository.updateState(reservation.id(), reservation.state());
    }

}
