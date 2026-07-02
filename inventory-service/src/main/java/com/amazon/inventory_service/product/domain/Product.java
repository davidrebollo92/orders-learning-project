package com.amazon.inventory_service.product.domain;

import com.amazon.inventory_service.product.domain.exception.InsufficientStockException;
import com.amazon.shared.core.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;

import java.util.UUID;

@Builder(access = AccessLevel.PRIVATE, setterPrefix = "with", toBuilder = true)
public record Product(UUID id, String name, Money price, int totalStock, int reservedStock) {

    public static Product create(String name, Money price, int totalStock) {
        return new Product(UUID.randomUUID(), name, price, totalStock, 0);
    }

    public int availableStock() {
        return totalStock - reservedStock;
    }

    public boolean hasStockFor(int quantity) {
        return quantity <= availableStock();
    }

    public Product reserve(int quantity) {
        if (!hasStockFor(quantity)) throw new InsufficientStockException();
        return toBuilder().withReservedStock(reservedStock + quantity).build();
    }

    public Product releaseReservation(int quantity) {
        return toBuilder().withReservedStock(reservedStock - quantity).build();
    }

    public Product confirmReservation(int quantity) {
        return toBuilder()
                .withTotalStock(totalStock - quantity)
                .withReservedStock(reservedStock - quantity)
                .build();
    }
}
