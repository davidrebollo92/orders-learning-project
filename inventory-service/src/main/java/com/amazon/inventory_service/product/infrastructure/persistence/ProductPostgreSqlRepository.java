package com.amazon.inventory_service.product.infrastructure.persistence;

import com.amazon.inventory_service.product.domain.Product;
import com.amazon.inventory_service.product.domain.ProductRepository;
import com.amazon.inventory_service.product.infrastructure.persistence.entity.ProductEntity;
import com.amazon.inventory_service.product.infrastructure.persistence.mapper.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductPostgreSqlRepository implements ProductRepository {
    private final JpaProductRepository jpaProductRepository;
    private final ProductEntityMapper productEntityMapper;

    @Override
    public Product save(Product product) {
        ProductEntity entity = productEntityMapper.toEntity(product);

        ProductEntity created = jpaProductRepository.save(entity);

        return productEntityMapper.toDomain(created);
    }

    @Override
    public List<Product> findAll() {
        return jpaProductRepository.findAll().stream()
                .map(productEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaProductRepository.findById(id)
                .map(productEntityMapper::toDomain);
    }

    @Override
    public void updateStock(Product product) {
        jpaProductRepository.updateStock(product.id(), product.totalStock(), product.reservedStock());
    }
}
