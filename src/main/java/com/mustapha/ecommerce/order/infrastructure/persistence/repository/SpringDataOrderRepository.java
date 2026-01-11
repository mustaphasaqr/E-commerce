package com.mustapha.ecommerce.order.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;

import java.util.List;

/**
 * Spring Data JPA Repository for Order
 */
@Repository
public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, String> {
    List<OrderJpaEntity> findByCustomerId(String customerId);
}
