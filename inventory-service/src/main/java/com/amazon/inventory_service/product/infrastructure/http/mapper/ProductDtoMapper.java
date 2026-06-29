package com.amazon.inventory_service.product.infrastructure.http.mapper;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.infrastructure.http.dto.CreateProductRequest;
import com.amazon.inventory_service.product.infrastructure.http.dto.ProductResponse;
import com.amazon.shared.core.domain.vo.Money;
import org.springframework.stereotype.Component;

@Component
public class ProductDtoMapper {

    public Product toDomain(CreateProductRequest request) {
        return Product.create(request.getName(), new Money(request.getPrice()), request.getStock());
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.price().amount(),
                product.totalStock(),
                product.reservedStock(),
                product.availableStock()
        );
    }
}
