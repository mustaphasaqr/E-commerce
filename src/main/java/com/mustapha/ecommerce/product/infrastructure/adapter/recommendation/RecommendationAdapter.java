package com.mustapha.ecommerce.product.infrastructure.adapter.recommendation;

import com.mustapha.ecommerce.product.application.port.RecommendationPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Rule-based recommendation engine
 * Future upgrade: Replace with ML model or AWS Personalize
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendationAdapter implements RecommendationPort {

    private final EntityManager entityManager;

    @Override
    public List<ProductRecommendation> getRecommendationsForCustomer(Long customerId, int limit) {
        log.info("🎯 Getting personalized recommendations for customer {}", customerId);

        // Strategy: Recommend based on customer's past purchases and views
        List<ProductRecommendation> recommendations = new ArrayList<>();

        // 1. Get products in same categories as past purchases
        recommendations.addAll(getRecommendationsBasedOnPurchaseHistory(customerId, limit));

        // 2. Get popular products customer hasn't bought
        if (recommendations.size() < limit) {
            recommendations.addAll(getTrendingProducts(limit - recommendations.size()));
        }

        log.info("✅ Generated {} recommendations for customer {}", recommendations.size(), customerId);
        return recommendations.stream().limit(limit).toList();
    }

    @Override
    public List<ProductRecommendation> getSimilarProducts(Long productId, int limit) {
        log.info("🔗 Getting similar products to product {}", productId);

        // Handle invalid limit
        if (limit <= 0) {
            log.warn("⚠️ Invalid limit {} requested, returning empty list", limit);
            return List.of();
        }

        // Strategy: Find products with similar price range
        String sql = """
            SELECT p2.id, p2.name, CAST(p2.price AS DOUBLE) as price,
                   ABS(p2.price - p1.price) as price_diff
            FROM products p1
            CROSS JOIN products p2
            WHERE p1.id = :productId
              AND p2.id != :productId
              AND ABS(p2.price - p1.price) < (p1.price * 0.5)
            ORDER BY price_diff ASC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("productId", productId);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<ProductRecommendation> recommendations = results.stream()
            .map(row -> new ProductRecommendation(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).doubleValue(),
                null,  // imageUrl not available in database
                0.8, // High confidence for similar products
                "Similar products with comparable pricing"
            ))
            .toList();

        log.info("✅ Found {} similar products", recommendations.size());
        return recommendations;
    }

    @Override
    public List<ProductRecommendation> getFrequentlyBoughtTogether(Long productId, int limit) {
        log.info("🛒 Getting frequently bought together with product {}", productId);

        // Handle invalid limit
        if (limit <= 0) {
            log.warn("⚠️ Invalid limit {} requested, returning empty list", limit);
            return List.of();
        }

        // Strategy: Find products that appear in same orders
        String sql = """
            SELECT p.id, p.name, CAST(p.price AS DOUBLE) as price, COUNT(*) as frequency
            FROM order_items oi1
            JOIN order_items oi2 ON oi1.order_id = oi2.order_id
            JOIN products p ON oi2.product_id = p.id
            WHERE oi1.product_id = :productId
              AND oi2.product_id != :productId
            GROUP BY p.id, p.name, p.price
            ORDER BY frequency DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("productId", productId);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<ProductRecommendation> recommendations = results.stream()
            .map(row -> new ProductRecommendation(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).doubleValue(),
                null,  // imageUrl not available in database
                0.9, // Very high confidence for co-purchased items
                "Customers who bought this also bought"
            ))
            .toList();

        log.info("✅ Found {} frequently bought together items", recommendations.size());
        return recommendations;
    }

    @Override
    public List<ProductRecommendation> getRecommendationsForCart(List<Long> productIds, int limit) {
        log.info("🛒 Getting recommendations for cart with {} items", productIds.size());

        if (productIds.isEmpty()) {
            return getTrendingProducts(limit);
        }

        // Strategy: Get frequently bought together for all cart items
        Set<ProductRecommendation> recommendations = new HashSet<>();

        for (Long productId : productIds) {
            recommendations.addAll(getFrequentlyBoughtTogether(productId, 3));
        }

        // Remove items already in cart
        List<ProductRecommendation> filtered = recommendations.stream()
            .filter(rec -> !productIds.contains(rec.productId()))
            .limit(limit)
            .toList();

        log.info("✅ Generated {} cart recommendations", filtered.size());
        return filtered;
    }

    @Override
    public List<ProductRecommendation> getTrendingProducts(int limit) {
        log.info("📈 Getting trending products");

        // Handle invalid limit
        if (limit <= 0) {
            log.warn("⚠️ Invalid limit {} requested, returning empty list", limit);
            return List.of();
        }

        // Strategy: Most sold products in last 30 days
        String sql = """
            SELECT p.id, p.name, CAST(p.price AS DOUBLE) as price, 
                   COUNT(oi.id) as recent_sales
            FROM products p
            JOIN order_items oi ON p.id = oi.product_id
            JOIN orders o ON oi.order_id = o.id
            WHERE o.created_at >= DATEADD('DAY', -30, NOW())
              AND o.status != 'CANCELLED'
            GROUP BY p.id, p.name, p.price
            ORDER BY recent_sales DESC
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        List<ProductRecommendation> recommendations = results.stream()
            .map(row -> new ProductRecommendation(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).doubleValue(),
                null,  // imageUrl not available in database
                0.7, // Medium confidence for trending
                "Trending now"
            ))
            .toList();

        log.info("✅ Found {} trending products", recommendations.size());
        return recommendations;
    }

    @Override
    public void trackProductView(Long customerId, Long productId) {
        log.debug("👁️ Customer {} viewed product {}", customerId, productId);
        // TODO: Store in customer_product_views table for personalization
        // For now, just log
    }

    @Override
    public void trackAddToCart(Long customerId, Long productId) {
        log.debug("🛒 Customer {} added product {} to cart", customerId, productId);
        // TODO: Store in customer_cart_events table for personalization
    }

    @Override
    public void trackPurchase(Long customerId, List<Long> productIds) {
        log.info("✅ Customer {} purchased {} products", customerId, productIds.size());
        // TODO: Update recommendation model with purchase data
    }

    private List<ProductRecommendation> getRecommendationsBasedOnPurchaseHistory(Long customerId, int limit) {
        // Get products not yet purchased by customer
        String sql = """
            SELECT p.id, p.name, CAST(p.price AS DOUBLE) as price
            FROM products p
            WHERE p.id NOT IN (
                SELECT oi2.product_id
                FROM orders o2
                JOIN order_items oi2 ON o2.id = oi2.order_id
                WHERE o2.customer_id = :customerId
            )
            LIMIT :limit
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("customerId", customerId);
        query.setParameter("limit", limit);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> new ProductRecommendation(
                ((Number) row[0]).longValue(),
                (String) row[1],
                ((Number) row[2]).doubleValue(),
                null,  // imageUrl not available in database
                0.75,
                "Based on your purchase history"
            ))
            .toList();
    }
}
