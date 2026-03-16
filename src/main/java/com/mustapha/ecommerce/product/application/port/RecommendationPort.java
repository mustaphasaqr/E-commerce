package com.mustapha.ecommerce.product.application.port;

import java.util.List;

/**
 * Port for product recommendation engine
 * Provides personalized product suggestions to increase conversion
 */
public interface RecommendationPort {

    /**
     * Get personalized recommendations for a customer
     */
    List<ProductRecommendation> getRecommendationsForCustomer(String customerId, int limit);

    /**
     * Get recommendations based on product (similar products)
     */
    List<ProductRecommendation> getSimilarProducts(String productId, int limit);

    /**
     * Get "Frequently Bought Together" recommendations
     */
    List<ProductRecommendation> getFrequentlyBoughtTogether(String productId, int limit);

    /**
     * Get recommendations based on cart contents
     */
    List<ProductRecommendation> getRecommendationsForCart(List<String> productIds, int limit);

    /**
     * Get trending/popular products
     */
    List<ProductRecommendation> getTrendingProducts(int limit);

    /**
     * Track product view (for personalization)
     */
    void trackProductView(String customerId, String productId);

    /**
     * Track add to cart event
     */
    void trackAddToCart(String customerId, String productId);

    /**
     * Track purchase event
     */
    void trackPurchase(String customerId, List<String> productIds);

    record ProductRecommendation(
        String productId,
        String productName,
        Double price,
        String imageUrl,
        Double confidence,  // 0.0 - 1.0 (how confident the recommendation is)
        String reason       // "Customers who bought X also bought Y", "Similar to products you viewed"
    ) {}
}
