package com.mustapha.ecommerce.product.application.port;

import java.util.List;

/**
 * Port for product search operations
 * Using PostgreSQL full-text search for fast, scalable product discovery
 */
public interface ProductSearchPort {

    /**
     * Full-text search across product name, description, tags
     * @param query Search query
     * @param offset Pagination offset
     * @param limit Max results
     * @return Search results with relevance score
     */
    SearchResults search(SearchQuery query);

    /**
     * Autocomplete suggestions for search input
     * @param prefix User input prefix
     * @param limit Max suggestions
     * @return List of suggested search terms
     */
    List<String> autocomplete(String prefix, int limit);

    /**
     * Search with advanced filters
     */
    SearchResults advancedSearch(AdvancedSearchQuery query);

    record SearchQuery(
        String query,
        int offset,
        int limit,
        SortBy sortBy
    ) {}

    record AdvancedSearchQuery(
        String query,
        List<Long> categoryIds,
        List<String> brands,
        PriceRange priceRange,
        Boolean inStockOnly,
        Integer minRating,
        int offset,
        int limit,
        SortBy sortBy
    ) {}

    record PriceRange(
        Double min,
        Double max
    ) {}

    enum SortBy {
        RELEVANCE,      // Best match first
        PRICE_ASC,      // Lowest price first
        PRICE_DESC,     // Highest price first
        NEWEST,         // Recently added first
        POPULARITY,     // Most sold first
        RATING          // Highest rated first
    }

    record SearchResults(
        List<ProductSearchResult> results,
        long totalResults,
        int offset,
        int limit,
        long searchTimeMs
    ) {}

    record ProductSearchResult(
        Long id,
        String name,
        String description,
        Double price,
        String imageUrl,
        String brand,
        Long categoryId,
        String categoryName,
        Double averageRating,
        int reviewCount,
        boolean inStock,
        double relevanceScore // 0.0 - 1.0
    ) {}
}
