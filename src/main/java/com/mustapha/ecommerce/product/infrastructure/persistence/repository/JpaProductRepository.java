package com.mustapha.ecommerce.product.infrastructure.persistence.repository;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Product Repository Implementation
 * Responsibility: Implement ProductRepository interface using JPA
 * Pattern: Repository, Adapter
 */
@Repository
public class JpaProductRepository implements ProductRepository {

    private final SpringDataProductRepository springDataRepository;
    private final ProductMapper mapper;
    
    public JpaProductRepository(SpringDataProductRepository springDataRepository, ProductMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductJpaEntity entity = mapper.toEntity(product);
        ProductJpaEntity saved = springDataRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return springDataRepository.findById(id.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySku(SKU sku) {
        return springDataRepository.findBySku(sku.getValue())
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsById(ProductId id) {
        return springDataRepository.existsById(id.getValue());
    }

    @Override
    public boolean existsBySku(SKU sku) {
        return springDataRepository.existsBySku(sku.getValue());
    }

    @Override
    public java.util.List<Product> findAll() {
        return springDataRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void deleteById(ProductId id) {
        springDataRepository.deleteById(id.getValue());
    }

    @Override
    public long count() {
        return springDataRepository.count();
    }
}
