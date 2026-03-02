package com.mustapha.ecommerce.product.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "rating", nullable = false)
    private int rating; // 1-5 stars

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "is_verified_purchase", nullable = false)
    private boolean isVerifiedPurchase;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount;

    @Column(name = "not_helpful_count", nullable = false)
    private int notHelpfulCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatus status;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReviewStatus {
        PENDING,    // Awaiting moderation
        APPROVED,   // Published
        REJECTED,   // Spam/inappropriate
        FLAGGED     // Reported by users
    }

    public void approve() {
        this.status = ReviewStatus.APPROVED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = ReviewStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    public void flag() {
        this.status = ReviewStatus.FLAGGED;
        this.updatedAt = LocalDateTime.now();
    }

    public void addHelpfulVote() {
        this.helpfulCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void addNotHelpfulVote() {
        this.notHelpfulCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAdminResponse(String response) {
        this.adminResponse = response;
        this.updatedAt = LocalDateTime.now();
    }

    public double getHelpfulPercentage() {
        int totalVotes = helpfulCount + notHelpfulCount;
        if (totalVotes == 0) return 0.0;
        return (double) helpfulCount / totalVotes * 100;
    }
}
