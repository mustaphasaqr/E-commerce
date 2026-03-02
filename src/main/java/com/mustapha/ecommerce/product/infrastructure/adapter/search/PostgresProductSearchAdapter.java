package com.mustapha.ecommerce.product.infrastructure.adapter.search;

import com.mustapha.ecommerce.product.application.port.ProductSearchPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL Full-Text Search adapter
 * Provides fast, scalable product search using native PostgreSQL features
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostgresProductSearchAdapter implements ProductSearchPort {

    private final EntityManager entityManager;

    @Override
    public SearchResults search(SearchQuery query) {
        long startTime = System.currentTimeMillis();
        
        log.info("🔍 Searching products: query='{}', offset={}, limit={}, sortBy={}", 
            query.query(), query.offset(), query.limit(), query.sortBy());

        // Build full-text search query
        String sql = buildSearchQuery(query.query(), query.sortBy(), false);
        
        Query jpaQuery = entityManager.createNativeQuery(sql, "ProductSearchResultMapping");
        jpaQuery.setParameter("query", preprocessQuery(query.query()));
        jpaQuery.setFirstResult(query.offset());
        jpaQuery.setMaxResults(query.limit());

        @SuppressWarnings("unchecked")
        List<Object[]> rawResults = jpaQuery.getResultList();

        // Count total results
        String countSql = buildSearchQuery(query.query(), query.sortBy(), true);
        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("query", preprocessQuery(query.query()));
        long totalResults = ((Number) countQuery.getSingleResult()).longValue();

        // Map results
        List<ProductSearchResult> results = rawResults.stream()
            .map(this::mapToSearchResult)
            .toList();

        long searchTimeMs = System.currentTimeMillis() - startTime;
        
        log.info("✅ Found {} results in {}ms", results.size(), searchTimeMs);

        return new SearchResults(results, totalResults, query.offset(), query.limit(), searchTimeMs);
    }

    @Override
    public List<String> autocomplete(String prefix, int limit) {
        log.debug("🔍 Autocomplete: prefix='{}', limit={}", prefix, limit);

        String sql = """
            SELECT DISTINCT p.name
            FROM products p
            WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))
            ORDER BY p.name
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("prefix", prefix);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<String> suggestions = query.getResultList();

        log.debug("✅ Found {} suggestions", suggestions.size());
        return suggestions;
    }

    @Override
    public SearchResults advancedSearch(AdvancedSearchQuery query) {
        long startTime = System.currentTimeMillis();
        
        log.info("🔍 Advanced search: query='{}', categories={}, brands={}, priceRange={}, inStock={}", 
            query.query(), query.categoryIds(), query.brands(), query.priceRange(), query.inStockOnly());

        String sql = buildAdvancedSearchQuery(query, false);
        Query jpaQuery = entityManager.createNativeQuery(sql, "ProductSearchResultMapping");
        
        setAdvancedSearchParameters(jpaQuery, query);
        jpaQuery.setFirstResult(query.offset());
        jpaQuery.setMaxResults(query.limit());

        @SuppressWarnings("unchecked")
        List<Object[]> rawResults = jpaQuery.getResultList();

        // Count total results
        String countSql = buildAdvancedSearchQuery(query, true);
        Query countQuery = entityManager.createNativeQuery(countSql);
        setAdvancedSearchParameters(countQuery, query);
        long totalResults = ((Number) countQuery.getSingleResult()).longValue();

        // Map results
        List<ProductSearchResult> results = rawResults.stream()
            .map(this::mapToSearchResult)
            .toList();

        long searchTimeMs = System.currentTimeMillis() - startTime;
        
        log.info("✅ Found {} results in {}ms", results.size(), searchTimeMs);

        return new SearchResults(results, totalResults, query.offset(), query.limit(), searchTimeMs);
    }

    private String buildSearchQuery(String query, SortBy sortBy, boolean isCount) {
        if (isCount) {
            return """
                SELECT COUNT(*)
                FROM products p
                WHERE to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, ''))
                      @@ to_tsquery('english', :query)
                """;
        }

        String selectClause = """
            SELECT p.id, p.name, p.description, p.price, p.image_url, p.brand, 
                   p.category_id, c.name AS category_name, 
                   COALESCE(AVG(r.rating), 0) AS average_rating,
                   COUNT(r.id) AS review_count,
                   CASE WHEN i.available_quantity > 0 THEN true ELSE false END AS in_stock,
                   ts_rank(to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, '')), 
                           to_tsquery('english', :query)) AS relevance_score
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            LEFT JOIN inventory i ON p.id = i.product_id
            LEFT JOIN product_reviews r ON p.id = r.product_id
            WHERE to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, ''))
                  @@ to_tsquery('english', :query)
            GROUP BY p.id, p.name, p.description, p.price, p.image_url, p.brand, 
                     p.category_id, c.name, i.available_quantity
            """;

        return selectClause + " " + buildOrderByClause(sortBy);
    }

    private String buildAdvancedSearchQuery(AdvancedSearchQuery query, boolean isCount) {
        StringBuilder sql = new StringBuilder();

        if (isCount) {
            sql.append("SELECT COUNT(*) FROM products p ");
        } else {
            sql.append("""
                SELECT p.id, p.name, p.description, p.price, p.image_url, p.brand, 
                       p.category_id, c.name AS category_name, 
                       COALESCE(AVG(r.rating), 0) AS average_rating,
                       COUNT(r.id) AS review_count,
                       CASE WHEN i.available_quantity > 0 THEN true ELSE false END AS in_stock,
                       ts_rank(to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, '')), 
                               to_tsquery('english', :query)) AS relevance_score
                FROM products p
                LEFT JOIN categories c ON p.category_id = c.id
                """);
        }

        sql.append("LEFT JOIN inventory i ON p.id = i.product_id ");
        sql.append("LEFT JOIN product_reviews r ON p.id = r.product_id ");
        
        sql.append("WHERE 1=1 ");

        // Text search
        if (query.query() != null && !query.query().isBlank()) {
            sql.append("AND to_tsvector('english', COALESCE(p.name, '') || ' ' || COALESCE(p.description, '')) ");
            sql.append("    @@ to_tsquery('english', :query) ");
        }

        // Category filter
        if (query.categoryIds() != null && !query.categoryIds().isEmpty()) {
            sql.append("AND p.category_id IN (:categoryIds) ");
        }

        // Brand filter
        if (query.brands() != null && !query.brands().isEmpty()) {
            sql.append("AND LOWER(p.brand) IN (:brands) ");
        }

        // Price range
        if (query.priceRange() != null) {
            if (query.priceRange().min() != null) {
                sql.append("AND p.price >= :minPrice ");
            }
            if (query.priceRange().max() != null) {
                sql.append("AND p.price <= :maxPrice ");
            }
        }

        // In stock filter
        if (query.inStockOnly() != null && query.inStockOnly()) {
            sql.append("AND i.available_quantity > 0 ");
        }

        if (!isCount) {
            sql.append("GROUP BY p.id, p.name, p.description, p.price, p.image_url, p.brand, ");
            sql.append("         p.category_id, c.name, i.available_quantity ");

            // Minimum rating filter (after grouping)
            if (query.minRating() != null) {
                sql.append("HAVING AVG(r.rating) >= :minRating ");
            }

            sql.append(buildOrderByClause(query.sortBy()));
        }

        return sql.toString();
    }

    private String buildOrderByClause(SortBy sortBy) {
        return switch (sortBy) {
            case RELEVANCE -> "ORDER BY relevance_score DESC, p.name ASC";
            case PRICE_ASC -> "ORDER BY p.price ASC, p.name ASC";
            case PRICE_DESC -> "ORDER BY p.price DESC, p.name ASC";
            case NEWEST -> "ORDER BY p.created_at DESC, p.name ASC";
            case POPULARITY -> "ORDER BY p.total_sales DESC, p.name ASC"; // Assuming total_sales column
            case RATING -> "ORDER BY average_rating DESC, review_count DESC, p.name ASC";
        };
    }

    private void setAdvancedSearchParameters(Query query, AdvancedSearchQuery searchQuery) {
        if (searchQuery.query() != null && !searchQuery.query().isBlank()) {
            query.setParameter("query", preprocessQuery(searchQuery.query()));
        }
        if (searchQuery.categoryIds() != null && !searchQuery.categoryIds().isEmpty()) {
            query.setParameter("categoryIds", searchQuery.categoryIds());
        }
        if (searchQuery.brands() != null && !searchQuery.brands().isEmpty()) {
            query.setParameter("brands", searchQuery.brands().stream()
                .map(String::toLowerCase)
                .toList());
        }
        if (searchQuery.priceRange() != null) {
            if (searchQuery.priceRange().min() != null) {
                query.setParameter("minPrice", searchQuery.priceRange().min());
            }
            if (searchQuery.priceRange().max() != null) {
                query.setParameter("maxPrice", searchQuery.priceRange().max());
            }
        }
        if (searchQuery.minRating() != null) {
            query.setParameter("minRating", searchQuery.minRating());
        }
    }

    private String preprocessQuery(String query) {
        // Convert to tsquery format: "search term" -> "search & term"
        return query.trim()
            .replaceAll("\\s+", " & ")  // Replace spaces with AND operator
            .replaceAll("[^a-zA-Z0-9& ]", ""); // Remove special chars
    }

    private ProductSearchResult mapToSearchResult(Object[] row) {
        int idx = 0;
        return new ProductSearchResult(
            ((Number) row[idx++]).longValue(),        // id
            (String) row[idx++],                       // name
            (String) row[idx++],                       // description
            ((Number) row[idx++]).doubleValue(),       // price
            (String) row[idx++],                       // imageUrl
            (String) row[idx++],                       // brand
            row[idx++] != null ? ((Number) row[idx-1]).longValue() : null, // categoryId
            (String) row[idx++],                       // categoryName
            ((Number) row[idx++]).doubleValue(),       // averageRating
            ((Number) row[idx++]).intValue(),          // reviewCount
            (Boolean) row[idx++],                      // inStock
            ((Number) row[idx++]).doubleValue()        // relevanceScore
        );
    }
}
