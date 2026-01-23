package com.mustapha.ecommerce.product.infrastructure.persistence.repository;

import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Product JPA Repository
 * Pattern: Spring Data JPA Repository
 * 
 * Provides CRUD operations for ProductJpaEntity
 */
@Repository
public interface SpingDataProductRepository extends JpaRepository<ProductJpaEntity, String> {

    /**
     * Find product by SKU
     * 
     * @param sku Product SKU
     * @return Optional product entity
     */
    Optional<ProductJpaEntity> findBySku(String sku);

    /**
     * Check if product exists by SKU
     * 
     * @param sku Product SKU
     * @return true if exists
     */
    boolean existsBySku(String sku);
}
