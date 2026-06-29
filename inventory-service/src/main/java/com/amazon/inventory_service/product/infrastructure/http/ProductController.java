package com.amazon.inventory_service.product.infrastructure.http;

import com.amazon.inventory_service.product.aplication.ProductCreator;
import com.amazon.inventory_service.product.aplication.ProductFinder;
import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.infrastructure.http.api.ProductsApi;
import com.amazon.inventory_service.product.infrastructure.http.dto.CreateProductRequest;
import com.amazon.inventory_service.product.infrastructure.http.dto.ProductResponse;
import com.amazon.inventory_service.product.infrastructure.http.mapper.ProductDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductsApi {

    private final ProductCreator productCreator;
    private final ProductFinder productFinder;
    private final ProductDtoMapper productDtoMapper;

    @Override
    public ResponseEntity<ProductResponse> createProduct(CreateProductRequest createProductRequest) {
        ProductResponse productResponse = productDtoMapper.toResponse(
                productCreator.create(productDtoMapper.toDomain(createProductRequest))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @Override
    public ResponseEntity<List<ProductResponse>> getProducts() {
        List<ProductResponse> productsResponse = productFinder.findAll().stream()
                .map(productDtoMapper::toResponse)
                .toList();

        return ResponseEntity.ok(productsResponse);
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(UUID id) {
        ProductResponse productResponse = productDtoMapper.toResponse(productFinder.findById(id));

        return ResponseEntity.ok(productResponse);
    }
}
