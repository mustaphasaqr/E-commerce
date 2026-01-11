package com.mustapha.ecommerce.order.infrastructure.persistence.repository;

import org.springframework.stereotype.Repository;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.mapper.OrderMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA Order Repository Implementation
 * Responsibility: Implement OrderRepository interface using JPA
 * Pattern: Repository, Adapter
 */
@Repository
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepository;
    private final OrderMapper orderMapper;

    public JpaOrderRepository(SpringDataOrderRepository springDataRepository, 
                             OrderMapper orderMapper) {
        this.springDataRepository = springDataRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderMapper.toEntity(order);
        OrderJpaEntity savedEntity = springDataRepository.save(entity);
        return orderMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(String id) {
        return springDataRepository.findById(id)
                .map(orderMapper::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return springDataRepository.findByCustomerId(customerId).stream()
                .map(orderMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        springDataRepository.deleteById(id);
    }
}
