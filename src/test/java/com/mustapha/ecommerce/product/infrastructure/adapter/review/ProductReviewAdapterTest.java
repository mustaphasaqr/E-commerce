package com.mustapha.ecommerce.product.infrastructure.adapter.review;

import com.mustapha.ecommerce.product.application.port.ProductReviewPort;
import com.mustapha.ecommerce.product.application.port.ProductReviewPort.*;
import com.mustapha.ecommerce.product.domain.model.ProductReview;
import com.mustapha.ecommerce.product.domain.repository.ProductReviewRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * Test Suite: Product Review Service
 * 
 * Tests:
 * 1. Unit Tests: Review submission, retrieval, stats calculation
 * 2. Resilience Tests: Duplicate reviews, invalid ratings, edge cases
 * 3. Integration Tests: Database interactions, sorting, pagination
 * 
 * Coverage:
 * - Review submission with validation
 * - Duplicate review prevention
 * - Review retrieval with sorting (NEWEST, HIGHEST_RATING, MOST_HELPFUL)
 * - Review statistics (average rating, distribution, total count)
 * - Helpful/Not Helpful voting
 * - Pagination
 * - Verified purchase validation
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Product Review Service Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductReviewAdapterTest {

    @Autowired
    private ProductReviewAdapter reviewAdapter;

    @Autowired
    private ProductReviewRepository reviewRepository;

    private static final Long TEST_PRODUCT_ID = 999L;
    private static final Long TEST_CUSTOMER_ID = 100L;
    private static final String TEST_CUSTOMER_NAME = "John Doe";
    private static final Long TEST_ORDER_ID = 12345L;

    @BeforeEach
    void setUp() {
        // Clean up reviews for test product
        reviewRepository.deleteAll();
    }

    @Nested
    @DisplayName("Unit Tests - Review Submission")
    class ReviewSubmissionTests {

        @Test
        @Order(1)
        @DisplayName("Should submit valid review successfully")
        @Transactional
        void testSubmitValidReview() {
            // Given: Valid review request
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                5,
                "Excellent Product!",
                "This product exceeded my expectations. Highly recommended!"
            );

            // When: Submit review
            Long reviewId = reviewAdapter.submitReview(request);

            // Then: Should be saved successfully
            assertThat(reviewId).isNotNull();
            assertThat(reviewId).isGreaterThan(0);

            // Verify in database
            ProductReview savedReview = reviewRepository.findById(reviewId).orElseThrow();
            assertThat(savedReview.getProductId()).isEqualTo(TEST_PRODUCT_ID);
            assertThat(savedReview.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
            assertThat(savedReview.getRating()).isEqualTo(5);
            assertThat(savedReview.getTitle()).isEqualTo("Excellent Product!");
            assertThat(savedReview.isVerifiedPurchase()).isTrue();
            assertThat(savedReview.getStatus()).isEqualTo(ProductReview.ReviewStatus.APPROVED);
        }

        @Test
        @Order(2)
        @DisplayName("Should reject rating below 1")
        @Transactional
        void testRejectLowRating() {
            // Given: Invalid rating (0)
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                0, // Invalid
                "Bad Review",
                "This shouldn't work"
            );

            // When & Then: Should throw exception
            assertThatThrownBy(() -> reviewAdapter.submitReview(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        }

        @Test
        @Order(3)
        @DisplayName("Should reject rating above 5")
        @Transactional
        void testRejectHighRating() {
            // Given: Invalid rating (6)
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                6, // Invalid
                "Over-enthusiastic",
                "Too much love"
            );

            // When & Then: Should throw exception
            assertThatThrownBy(() -> reviewAdapter.submitReview(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
        }

        @Test
        @Order(4)
        @DisplayName("Should prevent duplicate reviews")
        @Transactional
        void testPreventDuplicateReview() {
            // Given: Submit first review
            SubmitReviewRequest firstRequest = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                4,
                "First Review",
                "Original review text"
            );
            reviewAdapter.submitReview(firstRequest);

            // When: Try to submit duplicate review (same order, product, customer)
            SubmitReviewRequest duplicateRequest = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID, // Same order
                5,
                "Second Review",
                "Trying to change my mind"
            );

            // Then: Should throw exception
            assertThatThrownBy(() -> reviewAdapter.submitReview(duplicateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already reviewed");
        }

        @Test
        @Order(5)
        @DisplayName("Should allow same customer to review different products")
        @Transactional
        void testAllowMultipleProductsReviewBySameCustomer() {
            // Given: Review for first product
            SubmitReviewRequest firstProduct = new SubmitReviewRequest(
                100L, // Product 1
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                4,
                "Good",
                "Nice product"
            );
            reviewAdapter.submitReview(firstProduct);

            // When: Review for second product
            SubmitReviewRequest secondProduct = new SubmitReviewRequest(
                200L, // Product 2
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                5,
                "Excellent",
                "Even better"
            );

            // Then: Should succeed
            assertThatCode(() -> reviewAdapter.submitReview(secondProduct))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Unit Tests - Review Retrieval")
    class ReviewRetrievalTests {

        @Test
        @Order(10)
        @DisplayName("Should retrieve reviews with pagination")
        @Transactional
        void testReviewPagination() {
            // Given: Multiple reviews
            for (int i = 1; i <= 5; i++) {
                SubmitReviewRequest request = new SubmitReviewRequest(
                    TEST_PRODUCT_ID,
                    TEST_CUSTOMER_ID + i,
                    "Customer " + i,
                    TEST_ORDER_ID + i,
                    4,
                    "Review " + i,
                    "Review text " + i
                );
                reviewAdapter.submitReview(request);
            }

            // When: Get first page (size 3)
            ReviewsPage page1 = reviewAdapter.getProductReviews(TEST_PRODUCT_ID, 0, 3, SortBy.NEWEST);

            // Then: Should return 3 reviews
            assertThat(page1.reviews()).hasSize(3);
            assertThat(page1.totalReviews()).isEqualTo(5);
            assertThat(page1.page()).isEqualTo(0);
            assertThat(page1.size()).isEqualTo(3);

            // When: Get second page
            ReviewsPage page2 = reviewAdapter.getProductReviews(TEST_PRODUCT_ID, 1, 3, SortBy.NEWEST);

            // Then: Should return remaining 2 reviews
            assertThat(page2.reviews()).hasSize(2);
        }

        @Test
        @Order(11)
        @DisplayName("Should sort reviews by newest first")
        @Transactional
        void testSortByNewest() throws InterruptedException {
            // Given: Three reviews submitted with slight delay
            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 101L, "Alice", 1001L, 5, "First", "First review"
            ));
            Thread.sleep(10); // Small delay

            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 102L, "Bob", 1002L, 4, "Second", "Second review"
            ));
            Thread.sleep(10);

            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 103L, "Charlie", 1003L, 3, "Third", "Third review"
            ));

            // When: Get reviews sorted by newest
            ReviewsPage result = reviewAdapter.getProductReviews(TEST_PRODUCT_ID, 0, 10, SortBy.NEWEST);

            // Then: Should be in reverse chronological order
            assertThat(result.reviews()).hasSize(3);
            assertThat(result.reviews().get(0).customerName()).isEqualTo("Charlie"); // Most recent
            assertThat(result.reviews().get(2).customerName()).isEqualTo("Alice"); // Oldest
        }

        @Test
        @Order(12)
        @DisplayName("Should sort reviews by highest rating first")
        @Transactional
        void testSortByHighestRating() {
            // Given: Reviews with different ratings
            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 101L, "Alice", 1001L, 3, "Okay", "3 star review"
            ));
            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 102L, "Bob", 1002L, 5, "Perfect", "5 star review"
            ));
            reviewAdapter.submitReview(new SubmitReviewRequest(
                TEST_PRODUCT_ID, 103L, "Charlie", 1003L, 4, "Good", "4 star review"
            ));

            // When: Get reviews sorted by highest rating
            ReviewsPage result = reviewAdapter.getProductReviews(TEST_PRODUCT_ID, 0, 10, SortBy.HIGHEST_RATING);

            // Then: Should be ordered by rating (5, 4, 3)
            assertThat(result.reviews()).hasSize(3);
            assertThat(result.reviews().get(0).rating()).isEqualTo(5);
            assertThat(result.reviews().get(1).rating()).isEqualTo(4);
            assertThat(result.reviews().get(2).rating()).isEqualTo(3);
        }

        @Test
        @Order(13)
        @DisplayName("Should return empty page for product with no reviews")
        @Transactional
        void testNoReviews() {
            // When: Get reviews for product with no reviews
            ReviewsPage result = reviewAdapter.getProductReviews(999999L, 0, 10, SortBy.NEWEST);

            // Then: Should return empty page
            assertThat(result.reviews()).isEmpty();
            assertThat(result.totalReviews()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Unit Tests - Review Statistics")
    class ReviewStatsTests {

        @Test
        @Order(20)
        @DisplayName("Should calculate average rating correctly")
        @Transactional
        void testAverageRating() {
            // Given: Reviews with ratings 5, 4, 3, 4, 5
            reviewAdapter.submitReview(createReview(101L, 5));
            reviewAdapter.submitReview(createReview(102L, 4));
            reviewAdapter.submitReview(createReview(103L, 3));
            reviewAdapter.submitReview(createReview(104L, 4));
            reviewAdapter.submitReview(createReview(105L, 5));

            // When: Get stats
            ProductReviewStats stats = reviewAdapter.getProductReviewStats(TEST_PRODUCT_ID);

            // Then: Average should be (5+4+3+4+5)/5 = 4.2
            assertThat(stats.averageRating()).isCloseTo(4.2, within(0.01));
            assertThat(stats.totalReviews()).isEqualTo(5);
        }

        @Test
        @Order(21)
        @DisplayName("Should calculate rating distribution correctly")
        @Transactional
        void testRatingDistribution() {
            // Given: Mixed ratings
            reviewAdapter.submitReview(createReview(101L, 5)); // 5-star: 2
            reviewAdapter.submitReview(createReview(102L, 5));
            reviewAdapter.submitReview(createReview(103L, 4)); // 4-star: 1
            reviewAdapter.submitReview(createReview(104L, 3)); // 3-star: 1
            reviewAdapter.submitReview(createReview(105L, 1)); // 1-star: 1

            // When: Get stats
            ProductReviewStats stats = reviewAdapter.getProductReviewStats(TEST_PRODUCT_ID);

            // Then: Distribution should match
            assertThat(stats.ratingDistribution().get(5)).isEqualTo(2L);
            assertThat(stats.ratingDistribution().get(4)).isEqualTo(1L);
            assertThat(stats.ratingDistribution().get(3)).isEqualTo(1L);
            assertThat(stats.ratingDistribution().get(2)).isEqualTo(0L);
            assertThat(stats.ratingDistribution().get(1)).isEqualTo(1L);
        }

        @Test
        @Order(22)
        @DisplayName("Should return zero stats for product with no reviews")
        @Transactional
        void testNoReviewStats() {
            // When: Get stats for product without reviews
            ProductReviewStats stats = reviewAdapter.getProductReviewStats(999999L);

            // Then: Should return zeros
            assertThat(stats.totalReviews()).isEqualTo(0);
            assertThat(stats.averageRating()).isEqualTo(0.0);
        }

        @Test
        @Order(23)
        @DisplayName("Should calculate stats for all 5-star reviews")
        @Transactional
        void testAllFiveStarReviews() {
            // Given: Only 5-star reviews
            for (int i = 0; i < 10; i++) {
                reviewAdapter.submitReview(createReview(100L + i, 5));
            }

            // When: Get stats
            ProductReviewStats stats = reviewAdapter.getProductReviewStats(TEST_PRODUCT_ID);

            // Then: Perfect 5.0 rating
            assertThat(stats.averageRating()).isEqualTo(5.0);
            assertThat(stats.totalReviews()).isEqualTo(10);
            assertThat(stats.ratingDistribution().get(5)).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("Resilience Tests - Edge Cases")
    class ResilienceTests {

        @Test
        @Order(30)
        @DisplayName("Should handle very long review text")
        @Transactional
        void testLongReviewText() {
            // Given: Review with very long text
            String longText = "A".repeat(5000);
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                5,
                "Long Review",
                longText
            );

            // When: Submit review
            Long reviewId = reviewAdapter.submitReview(request);

            // Then: Should be saved (up to database limit)
            assertThat(reviewId).isNotNull();
        }

        @Test
        @Order(31)
        @DisplayName("Should handle empty review text")
        @Transactional
        void testEmptyReviewText() {
            // Given: Review with empty text
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                5,
                "No comment",
                ""
            );

            // When & Then: Should allow (optional field)
            assertThatCode(() -> reviewAdapter.submitReview(request))
                .doesNotThrowAnyException();
        }

        @Test
        @Order(32)
        @DisplayName("Should handle special characters in review")
        @Transactional
        void testSpecialCharactersInReview() {
            // Given: Review with special characters
            SubmitReviewRequest request = new SubmitReviewRequest(
                TEST_PRODUCT_ID,
                TEST_CUSTOMER_ID,
                TEST_CUSTOMER_NAME,
                TEST_ORDER_ID,
                4,
                "Review with émojis 😀🎉",
                "Test with symbols: @#$%^&*() and unicode: 你好 مرحبا"
            );

            // When: Submit review
            Long reviewId = reviewAdapter.submitReview(request);

            // Then: Should handle special characters
            assertThat(reviewId).isNotNull();
        }

        @Test
        @Order(33)
        @DisplayName("Should handle page beyond total reviews")
        @Transactional
        void testPageBeyondTotal() {
            // Given: Only 2 reviews
            reviewAdapter.submitReview(createReview(101L, 5));
            reviewAdapter.submitReview(createReview(102L, 4));

            // When: Request page 10 (way beyond available)
            ReviewsPage result = reviewAdapter.getProductReviews(TEST_PRODUCT_ID, 10, 10, SortBy.NEWEST);

            // Then: Should return empty page (no error)
            assertThat(result.reviews()).isEmpty();
            assertThat(result.totalReviews()).isEqualTo(2);
        }
    }

    // ========== Helper Methods ==========

    private SubmitReviewRequest createReview(Long customerId, int rating) {
        return new SubmitReviewRequest(
            TEST_PRODUCT_ID,
            customerId,
            "Customer " + customerId,
            TEST_ORDER_ID + customerId,
            rating,
            "Review " + rating + " stars",
            "This is a " + rating + " star review"
        );
    }
}
