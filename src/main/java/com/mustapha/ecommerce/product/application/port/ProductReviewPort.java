package com.mustapha.ecommerce.product.application.port;

import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Port for product review operations
 */
public interface ProductReviewPort {

    /**
     * Submit a new review (customer)
     * @return Review ID
     */
    Long submitReview(SubmitReviewRequest request);

    /**
     * Get approved reviews for a product
     */
    ReviewsPage getProductReviews(String productId, int page, int size, SortBy sortBy);

    /**
     * Get review statistics for a product
     */
    ProductReviewStats getProductReviewStats(String productId);

    /**
     * Mark review as helpful
     */
    void markHelpful(Long reviewId, String customerId);

    /**
     * Mark review as not helpful
     */
    void markNotHelpful(Long reviewId, String customerId);

    /**
     * Flag review for moderation
     */
    void flagReview(Long reviewId, String customerId, String reason);

    /**
     * Admin: Approve review
     */
    void approveReview(Long reviewId);

    /**
     * Admin: Reject review
     */
    void rejectReview(Long reviewId, String reason);

    /**
     * Admin: Add response to review
     */
    void addAdminResponse(Long reviewId, String response);

    /**
     * Send review reminder emails for delivered orders
     */
    void sendReviewReminders();

    record SubmitReviewRequest(
        String productId,
        String customerId,
        String customerName,
        Long orderId,
        int rating,  // 1-5
        String title,
        String reviewText
    ) {}

    record ReviewsPage(
        List<ReviewSummary> reviews,
        long totalReviews,
        int page,
        int size
    ) {}

    record ReviewSummary(
        Long id,
        String customerId,
        String customerName,
        int rating,
        String title,
        String reviewText,
        boolean isVerifiedPurchase,
        int helpfulCount,
        int notHelpfulCount,
        String adminResponse,
        LocalDateTime createdAt
    ) {}

    record ProductReviewStats(
        double averageRating,
        long totalReviews,
        Map<Integer, Long> ratingDistribution, // Star -> Count (5->100, 4->50, etc.)
        int verifiedPurchasePercentage
    ) {}

    enum SortBy {
        NEWEST,
        OLDEST,
        HIGHEST_RATING,
        LOWEST_RATING,
        MOST_HELPFUL
    }
}
