package com.mustapha.ecommerce.order.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Order
 */
@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, String> {
    
    @EntityGraph(attributePaths = {"items"})
    List<OrderJpaEntity> findByCustomerId(String customerId);
    
    @EntityGraph(attributePaths = {"items"})
    @Override
    Optional<OrderJpaEntity> findById(String id);
}
