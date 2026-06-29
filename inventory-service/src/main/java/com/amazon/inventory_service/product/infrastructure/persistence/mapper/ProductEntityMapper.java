package com.amazon.inventory_service.product.infrastructure.persistence.mapper;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.infrastructure.persistence.entity.ProductEntity;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityMapper {

    public ProductEntity toEntity(Product product) {

        ProductEntity entity = new ProductEntity();

        entity.setId(product.id());
        entity.setName(product.name());
        entity.setPrice(product.price().amount());
        entity.setTotalStock(product.totalStock());
        entity.setReservedStock(product.reservedStock());

        return entity;
    }

    public Product toDomain(ProductEntity entity) {

        return new Product(
                entity.getId(),
                entity.getName(),
                new Money(entity.getPrice()),
                entity.getTotalStock(),
                entity.getReservedStock()
        );
    }
}
