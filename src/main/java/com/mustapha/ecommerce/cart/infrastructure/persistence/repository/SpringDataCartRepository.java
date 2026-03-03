package com.mustapha.ecommerce.cart.infrastructure.persistence.repository;

import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Cart
 * Infrastructure Layer - Data Access
 * 
 * Provides database operations using Spring Data JPA conventions
 */
@Repository
public interface SpringDataCartRepository extends JpaRepository<CartJpaEntity, Long> {
    
    /**
     * Find active cart by user ID
     */
    Optional<CartJpaEntity> findByUserIdAndStatus(Long userId, CartStatusEntity status);
    
    /**
     * Find active cart by session ID
     */
    Optional<CartJpaEntity> findBySessionIdAndStatus(String sessionId, CartStatusEntity status);
    
    /**
     * Find all abandoned carts (for recovery campaigns)
     */
    List<CartJpaEntity> findByStatusAndLastUpdatedAtBefore(CartStatusEntity status, LocalDateTime before);
    
    /**
     * Mark carts as abandoned if inactive for > 24 hours
     */
    @Modifying
    @Query("UPDATE Cart c SET c.status = :abandonedStatus, c.lastUpdatedAt = :now " +
           "WHERE c.status = :activeStatus AND c.lastUpdatedAt < :cutoffTime")
    int markAbandonedCarts(
        @Param("activeStatus") CartStatusEntity activeStatus,
        @Param("abandonedStatus") CartStatusEntity abandonedStatus,
        @Param("cutoffTime") LocalDateTime cutoffTime,
        @Param("now") LocalDateTime now
    );
    
    /**
     * Count active carts for analytics
     */
    long countByStatus(CartStatusEntity status);
    
    /**
     * Find carts by user ID (all statuses)
     */
    List<CartJpaEntity> findByUserId(Long userId);
}
