package com.amazon.inventory_service.product.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity {

    @Id
    private UUID id;

    private String name;

    private BigDecimal price;

    private int totalStock;

    private int reservedStock;
}
