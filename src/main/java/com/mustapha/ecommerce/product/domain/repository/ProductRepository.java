package com.mustapha.ecommerce.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;

/**
 * Product Repository Interface - Domain Layer
 * Responsibility: Define contract for product persistence (Port in Hexagonal Architecture)
 * Pattern: Repository (abstraction)
 * SOLID: DIP (interface in domain, implementation in infrastructure)
 * 
 * Domain Rules:
 * - Uses ProductId and SKU value objects (not String) for type safety
 * - Returns Optional for single results (null-safe)
 * - All methods use domain objects (Product, ProductId, SKU)
 * - SKU uniqueness enforced via existsBySku
 */
public interface ProductRepository {
    /**
     * Save or update a product aggregate
     * After saving, infrastructure should publish domain events
     * @param product the product to save
     * @return the saved product (with updated version and state)
     */
    Product save(Product product);
    
    /**
     * Find product by its unique identifier
     * @param id the product ID
     * @return Optional containing product if found, empty otherwise
     */
    Optional<Product> findById(ProductId id);
    
    /**
     * Find product by its unique SKU
     * @param sku the product SKU
     * @return Optional containing product if found, empty otherwise
     */
    Optional<Product> findBySku(SKU sku);
    
    /**
     * Check if product exists by ID
     * @param id the product ID
     * @return true if product exists, false otherwise
     */
    boolean existsById(ProductId id);
    
    /**
     * Check if product exists by SKU (for uniqueness validation)
     * Use this before creating new products to ensure SKU uniqueness
     * @param sku the product SKU
     * @return true if SKU already exists, false otherwise
     */
    boolean existsBySku(SKU sku);
    
    /**
     * Find all products
     * Note: Consider pagination for production use
     * @return list of all products (empty list if none found)
     */
    List<Product> findAll();
    
    /**
     * Delete a product by its identifier
     * Use with caution - prefer discontinue() for audit trail
     * @param id the product ID to delete
     */
    void deleteById(ProductId id);
    
    /**
     * Get total count of products
     * @return total number of products
     */
    long count();
}
