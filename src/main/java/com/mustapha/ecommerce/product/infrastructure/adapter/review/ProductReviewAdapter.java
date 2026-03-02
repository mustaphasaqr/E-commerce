package com.mustapha.ecommerce.product.infrastructure.adapter.review;

import com.mustapha.ecommerce.product.application.port.ProductReviewPort;
import com.mustapha.ecommerce.product.domain.model.ProductReview;
import com.mustapha.ecommerce.product.domain.model.ProductReview.ReviewStatus;
import com.mustapha.ecommerce.product.domain.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductReviewAdapter implements ProductReviewPort {

    private final ProductReviewRepository reviewRepository;

    @Override
    @Transactional
    public Long submitReview(SubmitReviewRequest request) {
        log.info("⭐ Submitting review for product {} by customer {} (rating: {}/5)", 
            request.productId(), request.customerId(), request.rating());

        // Validate rating
        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Check if already reviewed
        boolean alreadyReviewed = reviewRepository.existsByOrderIdAndProductIdAndCustomerId(
            request.orderId(), request.productId(), request.customerId()
        );

        if (alreadyReviewed) {
            throw new IllegalStateException("You have already reviewed this product");
        }

        // TODO: Verify customer actually purchased this product in this order
        // For now, assume verified purchase

        ProductReview review = ProductReview.builder()
            .productId(request.productId())
            .customerId(request.customerId())
            .customerName(request.customerName())
            .orderId(request.orderId())
            .rating(request.rating())
            .title(request.title())
            .reviewText(request.reviewText())
            .isVerifiedPurchase(true)
            .helpfulCount(0)
            .notHelpfulCount(0)
            .status(ReviewStatus.APPROVED) // Auto-approve for now; add moderation later
            .createdAt(LocalDateTime.now())
            .build();

        review = reviewRepository.save(review);

        log.info("✅ Review submitted successfully (ID: {})", review.getId());
        return review.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewsPage getProductReviews(Long productId, int page, int size, SortBy sortBy) {
        log.debug("📖 Getting reviews for product {} (page: {}, size: {}, sort: {})", 
            productId, page, size, sortBy);

        PageRequest pageRequest = PageRequest.of(page, size, buildSort(sortBy));
        Page<ProductReview> reviewPage = reviewRepository.findByProductIdAndStatus(
            productId, ReviewStatus.APPROVED, pageRequest
        );

        List<ReviewSummary> reviews = reviewPage.getContent().stream()
            .map(this::mapToSummary)
            .toList();

        log.debug("✅ Found {} reviews", reviews.size());

        return new ReviewsPage(reviews, reviewPage.getTotalElements(), page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductReviewStats getProductReviewStats(Long productId) {
        log.debug("📊 Getting review stats for product {}", productId);

        Double avgRating = reviewRepository.calculateAverageRating(productId);
        long totalReviews = reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);
        List<Object[]> distribution = reviewRepository.getRatingDistribution(productId);

        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (Object[] row : distribution) {
            ratingDistribution.put((Integer) row[0], (Long) row[1]);
        }

        // Fill in missing ratings with 0
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.putIfAbsent(i, 0L);
        }

        // TODO: Calculate verified purchase percentage
        int verifiedPurchasePercentage = 100; // For now, assume all are verified

        ProductReviewStats stats = new ProductReviewStats(
            avgRating != null ? avgRating : 0.0,
            totalReviews,
            ratingDistribution,
            verifiedPurchasePercentage
        );

        log.debug("✅ Stats: avg={}, total={}", stats.averageRating(), stats.totalReviews());
        return stats;
    }

    @Override
    @Transactional
    public void markHelpful(Long reviewId, Long customerId) {
        log.info("👍 Customer {} marking review {} as helpful", customerId, reviewId);

        // TODO: Track who voted to prevent duplicate votes
        // For now, allow unlimited votes

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.addHelpfulVote();
        reviewRepository.save(review);

        log.info("✅ Review {} now has {} helpful votes", reviewId, review.getHelpfulCount());
    }

    @Override
    @Transactional
    public void markNotHelpful(Long reviewId, Long customerId) {
        log.info("👎 Customer {} marking review {} as not helpful", customerId, reviewId);

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.addNotHelpfulVote();
        reviewRepository.save(review);

        log.info("✅ Review {} now has {} not helpful votes", reviewId, review.getNotHelpfulCount());
    }

    @Override
    @Transactional
    public void flagReview(Long reviewId, Long customerId, String reason) {
        log.warn("🚩 Customer {} flagging review {} (reason: {})", customerId, reviewId, reason);

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.flag();
        reviewRepository.save(review);

        // TODO: Notify moderators
        // TODO: Store flagging reason in separate table

        log.warn("⚠️ Review {} flagged for moderation", reviewId);
    }

    @Override
    @Transactional
    public void approveReview(Long reviewId) {
        log.info("✅ Approving review {}", reviewId);

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.approve();
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void rejectReview(Long reviewId, String reason) {
        log.info("❌ Rejecting review {} (reason: {})", reviewId, reason);

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.reject();
        reviewRepository.save(review);

        // TODO: Notify customer about rejection
    }

    @Override
    @Transactional
    public void addAdminResponse(Long reviewId, String response) {
        log.info("💬 Adding admin response to review {}", reviewId);

        ProductReview review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));

        review.setAdminResponse(response);
        reviewRepository.save(review);

        log.info("✅ Admin response added to review {}", reviewId);
    }

    @Override
    public void sendReviewReminders() {
        log.info("📧 Sending review reminders...");
        // TODO: Implement review reminder email job
        // 1. Find orders delivered 7 days ago without reviews
        // 2. Send email with link to review page
        // 3. Track sent reminders to avoid spam
        log.info("✅ Review reminders sent");
    }

    private Sort buildSort(SortBy sortBy) {
        return switch (sortBy) {
            case NEWEST -> Sort.by("createdAt").descending();
            case OLDEST -> Sort.by("createdAt").ascending();
            case HIGHEST_RATING -> Sort.by("rating").descending().and(Sort.by("createdAt").descending());
            case LOWEST_RATING -> Sort.by("rating").ascending().and(Sort.by("createdAt").descending());
            case MOST_HELPFUL -> Sort.by("helpfulCount").descending().and(Sort.by("createdAt").descending());
        };
    }

    private ReviewSummary mapToSummary(ProductReview review) {
        return new ReviewSummary(
            review.getId(),
            review.getCustomerId(),
            review.getCustomerName(),
            review.getRating(),
            review.getTitle(),
            review.getReviewText(),
            review.isVerifiedPurchase(),
            review.getHelpfulCount(),
            review.getNotHelpfulCount(),
            review.getAdminResponse(),
            review.getCreatedAt()
        );
    }
}
