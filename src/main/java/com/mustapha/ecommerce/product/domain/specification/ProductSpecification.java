package com.mustapha.ecommerce.product.domain.specification;

import com.mustapha.ecommerce.product.domain.model.Product;

/**
 * Product Specification Interface
 * 
 * Pattern: Specification Pattern
 * Purpose: Encapsulate business rules for querying/filtering products
 * 
 * Benefits:
 * - Single Responsibility: Each specification has ONE query rule
 * - Open/Closed: Add new queries without changing repository
 * - Testable: Test specifications independently
 * - Reusable: Combine specifications (AND, OR, NOT)
 * - Domain Language: Queries expressed in business terms
 * 
 * Usage Example:
 * <pre>
 * ProductRepository repo = ...;
 * 
 * // Simple query
 * List<Product> availableProducts = repo.findBySpecification(
 *     new AvailableProductsSpecification()
 * );
 * 
 * // Combined query (future)
 * List<Product> inStockLowPrice = repo.findBySpecification(
 *     new InStockSpecification().and(
 *         new PriceRangeSpecification(0, 50)
 *     )
 * );
 * </pre>
 * 
 * When to Use:
 * ✅ Complex filtering logic
 * ✅ Need to combine multiple criteria dynamically
 * ✅ Business rules for querying are complex
 * 
 * When NOT to Use:
 * ❌ Simple queries (findById, findBySku)
 * ❌ Static queries that never change
 */
public interface ProductSpecification {
    
    /**
     * Check if a product satisfies this specification
     * 
     * @param product the product to test
     * @return true if product matches criteria
     */
    boolean isSatisfiedBy(Product product);
}
