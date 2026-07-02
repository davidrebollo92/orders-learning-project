package com.amazon.inventory_service.product.infrastructure.persistence;

import com.amazon.inventory_service.product.infrastructure.persistence.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    Optional<ProductEntity> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE ProductEntity p SET p.totalStock = :totalStock, p.reservedStock = :reservedStock WHERE p.id = :id")
    void updateStock(@Param("id") UUID id, @Param("totalStock") int totalStock, @Param("reservedStock") int reservedStock);
}
