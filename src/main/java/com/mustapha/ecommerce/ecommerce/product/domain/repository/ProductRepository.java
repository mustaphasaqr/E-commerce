package com.mustapha.ecommerce.ecommerce.product.domain.repository;

import java.util.List;
import java.util.Optional;

import com.mustapha.ecommerce.ecommerce.product.domain.model.Product;

/**
 * Product Repository Interface
 */
public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    List<Product> findAll();
    void delete(String id);
}
