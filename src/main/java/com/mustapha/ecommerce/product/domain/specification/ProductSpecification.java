package com.mustapha.ecommerce.product.domain.specification;

import com.mustapha.ecommerce.product.domain.model.Product;

/**
 * Product Specification Interface
 * 
 * Pattern: Specification Pattern (DDD)
 * Purpose: Encapsulate business rules for querying/filtering products
 * 
 * Benefits:
 * - Single Responsibility: Each specification has ONE query rule
 * - Open/Closed: Add new queries without changing repository
 * - Testable: Test specifications independently
 * - Reusable: Combine specifications (AND, OR, NOT)
 * - Domain Language: Queries expressed in business terms
 * 
 * Current Status: PREPARED FOR FUTURE USE
 * - Pattern implemented and ready
 * - Not yet integrated with repository (MVP uses simple queries)
 * - Can be activated when complex filtering requirements emerge
 * 
 * Usage Example (Future):
 * <pre>
 * ProductRepository repo = ...;
 * 
 * // Simple query
 * List<Product> availableProducts = repo.findBySpecification(
 *     new InStockSpecification()
 * );
 * 
 * // Combined query (when needed)
 * List<Product> inStockLowPrice = repo.findBySpecification(
 *     new InStockSpecification().and(
 *         new PriceRangeSpecification(minPrice, maxPrice)
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
 * ❌ Simple queries (findById, findBySku) - use repository methods directly
 * ❌ Static queries that never change
 * 
 * Implementation Note:
 * Per YAGNI principle, repository integration deferred until complex query needs arise.
 * Current MVP satisfied by ProductRepository's basic query methods.
 */
public interface ProductSpecification {
    
    /**
     * Check if a product satisfies this specification
     * 
     * @param product The product to check
     * @return true if product satisfies the specification, false otherwise
     */
    boolean isSatisfiedBy(Product product);
    
    // Future: Add combinators (AND, OR, NOT) when complex queries needed
    // default ProductSpecification and(ProductSpecification other) { ... }
    // default ProductSpecification or(ProductSpecification other) { ... }
    // default ProductSpecification not() { ... }
}
