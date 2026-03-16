package com.mustapha.ecommerce.product.domain.repository;

import com.mustapha.ecommerce.product.domain.model.ProductReview;
import com.mustapha.ecommerce.product.domain.model.ProductReview.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    /**
     * Find all approved reviews for a product
     */
    Page<ProductReview> findByProductIdAndStatus(String productId, ReviewStatus status, Pageable pageable);

    /**
     * Find customer's review for a specific product in an order
     */
    Optional<ProductReview> findByOrderIdAndProductIdAndCustomerId(
        Long orderId, String productId, String customerId
    );

    /**
     * Check if customer already reviewed a product in an order
     */
    boolean existsByOrderIdAndProductIdAndCustomerId(Long orderId, String productId, String customerId);

    /**
     * Get all reviews by customer
     */
    List<ProductReview> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    /**
     * Get pending reviews for moderation
     */
    Page<ProductReview> findByStatus(ReviewStatus status, Pageable pageable);

    /**
     * Calculate average rating for a product
     */
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Double calculateAverageRating(@Param("productId") String productId);

    /**
     * Count approved reviews for a product
     */
    long countByProductIdAndStatus(String productId, ReviewStatus status);

    /**
     * Get rating distribution for a product
     */
    @Query("SELECT r.rating, COUNT(r) FROM ProductReview r " +
           "WHERE r.productId = :productId AND r.status = 'APPROVED' " +
           "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") String productId);
}
