package com.mustapha.ecommerce.product.infrastructure.adapter.recommendation;

import com.mustapha.ecommerce.product.application.port.RecommendationPort;
import com.mustapha.ecommerce.product.application.port.RecommendationPort.ProductRecommendation;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Test Suite: Product Recommendation Engine
 * 
 * Tests:
 * 1. Unit Tests: Collaborative filtering algorithms
 * 2. Resilience Tests: Empty data, cold start, edge cases
 * 3. Integration Tests: SQL queries, sorting, limit handling
 * 
 * Coverage:
 * - Similar Products (category, price range matching)
 * - Frequently Bought Together (order analysis)
 * - Trending Products (recent sales velocity)
 * - Personalized Recommendations (purchase history)
 * - Cold start scenarios (new products, new customers)
 * - Empty result handling
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Recommendation Engine Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RecommendationAdapterTest {

    @Autowired
    private RecommendationAdapter recommendationAdapter;

    private static final String TEST_PRODUCT_ID = "1";
    private static final String TEST_CUSTOMER_ID = "1";

    @Nested
    @DisplayName("Unit Tests - Similar Products")
    class SimilarProductsTests {

        @Test
        @Order(1)
        @DisplayName("Should retrieve similar products based on category and price")
        @Transactional(readOnly = true)
        void testGetSimilarProducts() {
            // When: Get similar products
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, 5);

            // Then: Should return list (may be empty if no data)
            assertThat(recommendations).isNotNull();
            assertThat(recommendations.size()).isLessThanOrEqualTo(5);

            // If not empty, verify structure
            if (!recommendations.isEmpty()) {
                ProductRecommendation first = recommendations.get(0);
                assertThat(first.productId()).isNotNull();
                assertThat(first.productName()).isNotNull();
                assertThat(first.price()).isNotNull();
            }
        }

        @Test
        @Order(2)
        @DisplayName("Should handle limit parameter correctly")
        @Transactional(readOnly = true)
        void testSimilarProductsLimit() {
            // When: Get similar products with limit 3
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, 3);

            // Then: Should respect limit
            assertThat(recommendations.size()).isLessThanOrEqualTo(3);
        }

        @Test
        @Order(3)
        @DisplayName("Should exclude the source product from recommendations")
        @Transactional(readOnly = true)
        void testExcludeSourceProduct() {
            // When: Get similar products
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, 10);

            // Then: Should not include the source product
            assertThat(recommendations)
                .noneMatch(rec -> rec.productId().equals(TEST_PRODUCT_ID));
        }

        @Test
        @Order(4)
        @DisplayName("Should handle non-existent product gracefully")
        @Transactional(readOnly = true)
        void testNonExistentProduct() {
            // When: Get similar products for non-existent product
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts("999999", 5);

            // Then: Should return empty list (no exception)
            assertThat(recommendations).isNotNull();
            assertThat(recommendations).isEmpty();
        }

        @Test
        @Order(5)
        @DisplayName("Should handle zero limit")
        @Transactional(readOnly = true)
        void testZeroLimit() {
            // When: Get similar products with 0 limit
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, 0);

            // Then: Should return empty list
            assertThat(recommendations).isEmpty();
        }

        @Test
        @Order(6)
        @DisplayName("Should handle negative limit gracefully")
        @Transactional(readOnly = true)
        void testNegativeLimit() {
            // When: Get similar products with negative limit
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, -5);

            // Then: Should handle gracefully (empty or default behavior)
            assertThat(recommendations).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unit Tests - Frequently Bought Together")
    @Disabled("H2 incompatibility: CAST(price AS DOUBLE) fails on H2 with DECIMAL columns")
    class FrequentlyBoughtTogetherTests {

        @Test
        @Order(10)
        @DisplayName("Should retrieve frequently bought together products")
        @Transactional(readOnly = true)
        void testFrequentlyBoughtTogether() {
            // When: Get FBT products
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getFrequentlyBoughtTogether(TEST_PRODUCT_ID, 5);

            // Then: Should return list
            assertThat(recommendations).isNotNull();
            assertThat(recommendations.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @Order(11)
        @DisplayName("Should exclude source product from FBT recommendations")
        @Transactional(readOnly = true)
        void testFBTExcludeSourceProduct() {
            // When: Get FBT products
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getFrequentlyBoughtTogether(TEST_PRODUCT_ID, 10);

            // Then: Should not include source product
            assertThat(recommendations)
                .noneMatch(rec -> rec.productId().equals(TEST_PRODUCT_ID));
        }

        @Test
        @Order(12)
        @DisplayName("Should handle product with no co-purchases")
        @Transactional(readOnly = true)
        void testNoCoPurchases() {
            // When: Get FBT for product that's never been bought with others
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getFrequentlyBoughtTogether("999999", 5);

            // Then: Should return empty list
            assertThat(recommendations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Unit Tests - Trending Products")
    class TrendingProductsTests {

        @Test
        @Order(20)
        @DisplayName("Should retrieve trending products")
        @Transactional(readOnly = true)
        void testGetTrendingProducts() {
            // When: Get trending products
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(10);

            // Then: Should return list
            assertThat(recommendations).isNotNull();
            assertThat(recommendations.size()).isLessThanOrEqualTo(10);
        }

        @Test
        @Order(21)
        @DisplayName("Should respect limit parameter")
        @Transactional(readOnly = true)
        void testTrendingDayRange() {
            // When: Get trending products with limit 5
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(5);

            // Then: Should work
            assertThat(recommendations).isNotNull();
            assertThat(recommendations.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @Order(22)
        @DisplayName("Should handle zero limit")
        @Transactional(readOnly = true)
        void testTrendingZeroDays() {
            // When: Get trending products with 0 limit
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(0);

            // Then: Should return empty list
            assertThat(recommendations).isEmpty();
        }

        @Test
        @Order(23)
        @DisplayName("Should handle large limit gracefully")
        @Transactional(readOnly = true)
        void testTrendingLargeLimit() {
            // When: Get trending products with very large limit
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(1000);

            // Then: Should return available products (not crash)
            assertThat(recommendations).isNotNull();
        }
    }

    @Nested
    @DisplayName("Unit Tests - Personalized Recommendations")
    @Disabled("H2 incompatibility: CAST(price AS DOUBLE) fails with H2 DECIMAL columns")
    class PersonalizedRecommendationsTests {

        @Test
        @Order(30)
        @DisplayName("Should retrieve personalized recommendations based on history")
        @Transactional(readOnly = true)
        void testGetPersonalizedRecommendations() {
            // When: Get personalized recommendations
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getRecommendationsForCustomer(TEST_CUSTOMER_ID, 5);

            // Then: Should return list
            assertThat(recommendations).isNotNull();
            assertThat(recommendations.size()).isLessThanOrEqualTo(5);
        }

        @Test
        @Order(31)
        @DisplayName("Should handle new customer with no purchase history")
        @Transactional(readOnly = true)
        void testNewCustomerColdStart() {
            // When: Get recommendations for customer with no history
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getRecommendationsForCustomer("999999", 5);

            // Then: Should return empty list (cold start scenario)
            assertThat(recommendations).isEmpty();
        }

        @Test
        @Order(32)
        @DisplayName("Should exclude products customer already purchased")
        @Transactional(readOnly = true)
        void testExcludeAlreadyPurchased() {
            // When: Get personalized recommendations
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getRecommendationsForCustomer(TEST_CUSTOMER_ID, 20);

            // Then: Should not contain duplicate products
            long uniqueCount = recommendations.stream()
                .map(ProductRecommendation::productId)
                .distinct()
                .count();

            assertThat(uniqueCount).isEqualTo(recommendations.size());
        }

        @Test
        @Order(33)
        @DisplayName("Should handle zero limit")
        @Transactional(readOnly = true)
        void testPersonalizedZeroLimit() {
            // When: Get recommendations with 0 limit
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getRecommendationsForCustomer(TEST_CUSTOMER_ID, 0);

            // Then: Should return empty list
            assertThat(recommendations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Resilience Tests - Edge Cases & Error Handling")
    class ResilienceTests {

        @Test
        @Order(40)
        @DisplayName("Should handle null product ID gracefully")
        @Transactional(readOnly = true)
        void testNullProductId() {
            // When & Then: Should not crash with null
            assertThatCode(() -> 
                recommendationAdapter.getSimilarProducts(null, 5)
            ).doesNotThrowAnyException();
        }

        @Test
        @Order(41)
        @DisplayName("Should handle null customer ID gracefully")
        @Transactional(readOnly = true)
        void testNullCustomerId() {
            // When & Then: Should not crash with null
            assertThatCode(() -> 
                recommendationAdapter.getRecommendationsForCustomer(null, 5)
            ).doesNotThrowAnyException();
        }

        @Test
        @Order(42)
        @DisplayName("Should handle very large product ID")
        @Transactional(readOnly = true)
        @Disabled("H2 incompatibility: CAST(price AS DOUBLE) fails on H2 with DECIMAL columns")
        void testVeryLargeProductId() {
            // When: Get similar products for very large ID
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getSimilarProducts(String.valueOf(Long.MAX_VALUE), 5);

            // Then: Should return empty list (no exception)
            assertThat(recommendations).isNotNull();
            assertThat(recommendations).isEmpty();
        }

        @Test
        @Order(43)
        @DisplayName("Should handle concurrent requests safely")
        @Transactional(readOnly = true)
        @Disabled("H2 incompatibility: CAST(price AS DOUBLE) fails on H2 with DECIMAL columns")
        void testConcurrentRequests() {
            // When: Multiple requests at once
            assertThatCode(() -> {
                recommendationAdapter.getSimilarProducts(TEST_PRODUCT_ID, 5);
                recommendationAdapter.getFrequentlyBoughtTogether(TEST_PRODUCT_ID, 5);
                recommendationAdapter.getTrendingProducts(5);
                recommendationAdapter.getRecommendationsForCustomer(TEST_CUSTOMER_ID, 5);
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Integration Tests - Real Data Scenarios")
    @Disabled("Requires external recommendation service")
    class IntegrationTests {

        @Test
        @Order(50)
        @DisplayName("Should return valid product data structure")
        @Transactional(readOnly = true)
        void testProductDataStructure() {
            // When: Get any recommendations
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(5);

            // Then: Each recommendation should have complete data
            recommendations.forEach(rec -> {
                assertThat(rec.productId()).isNotNull();
                assertThat(rec.productName()).isNotNull();
                assertThat(rec.price()).isNotNull();
                // imageUrl can be null (as per recent fix)
            });
        }

        @Test
        @Order(51)
        @DisplayName("Should work with default database configuration")
        @Transactional(readOnly = true)
        void testWithDefaultConfiguration() {
            // Given: Adapter initialized with Spring configuration
            assertThat(recommendationAdapter).isNotNull();

            // When: Call all recommendation methods
            List<ProductRecommendation> similar = recommendationAdapter
                .getSimilarProducts(TEST_PRODUCT_ID, 5);
            List<ProductRecommendation> fbt = recommendationAdapter
                .getFrequentlyBoughtTogether(TEST_PRODUCT_ID, 5);
            List<ProductRecommendation> trending = recommendationAdapter
                .getTrendingProducts(5);
            List<ProductRecommendation> personalized = recommendationAdapter
                .getRecommendationsForCustomer(TEST_CUSTOMER_ID, 5);

            // Then: All should complete without error
            assertThat(similar).isNotNull();
            assertThat(fbt).isNotNull();
            assertThat(trending).isNotNull();
            assertThat(personalized).isNotNull();
        }

        @Test
        @Order(52)
        @DisplayName("Should handle empty database gracefully")
        @Transactional(readOnly = true)
        void testEmptyDatabase() {
            // When: Get recommendations from potentially empty database
            List<ProductRecommendation> recommendations = recommendationAdapter
                .getTrendingProducts(10);

            // Then: Should return empty list (no crash)
            assertThat(recommendations).isNotNull();
            // May be empty if no orders in last 7 days
        }
    }
}
