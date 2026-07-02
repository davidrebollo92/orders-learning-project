package com.amazon.inventory_service.reservation.infrastructure.persistence.mapper;

import com.amazon.inventory_service.reservation.domain.Reservation;
import com.amazon.inventory_service.reservation.infrastructure.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

@Component
public class ReservationEntityMapper {

    public ReservationEntity toEntity(Reservation reservation) {

        ReservationEntity entity = new ReservationEntity();

        entity.setId(reservation.id());
        entity.setOrderId(reservation.orderId());
        entity.setProductId(reservation.productId());
        entity.setQuantity(reservation.quantity());
        entity.setState(reservation.state());
        entity.setCreatedAt(reservation.createdAt());

        return entity;
    }

    public Reservation toDomain(ReservationEntity entity) {

        return new Reservation(
                entity.getId(),
                entity.getOrderId(),
                entity.getProductId(),
                entity.getQuantity(),
                entity.getState(),
                entity.getCreatedAt()
        );
    }
}
