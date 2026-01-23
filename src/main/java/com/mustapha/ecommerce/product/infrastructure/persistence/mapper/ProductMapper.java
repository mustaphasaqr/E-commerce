package com.mustapha.ecommerce.product.infrastructure.persistence.mapper;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.*;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product Mapper
 * Pattern: Mapper (Anti-Corruption Layer)
 * 
 * Converts between domain aggregate and JPA entity
 */
@Component
public class ProductMapper {

    /**
     * Convert domain aggregate to JPA entity
     * 
     * @param product Domain aggregate
     * @return JPA entity
     */
    public ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        
        entity.setId(product.getId().getValue());
        entity.setSku(product.getSku().getValue());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setPrice(product.getPrice().getAmount());
        entity.setCurrency(product.getPrice().getCurrency().getCurrencyCode());
        entity.setTotalStock(product.getStock().getQuantity());
        entity.setAvailableStock(product.getStock().getAvailableQuantity());
        entity.setReservedStock(product.getStock().getReservedQuantity());
        entity.setActive(product.isActive());
        entity.setVisible(product.isVisible());
        entity.setAvailableForPurchase(product.isAvailableForPurchase());
        entity.setDiscontinued(product.isDiscontinued());
        entity.setCreatedAt(product.getCreatedAt());
        entity.setUpdatedAt(product.getUpdatedAt());
        
        // Map reservations
        Map<String, Integer> reservationMap = product.getStock()
            .getReservations()
            .values()
            .stream()
            .collect(Collectors.toMap(
                Reservation::getOrderId,
                Reservation::getQuantity
            ));
        entity.setReservations(reservationMap);
        
        return entity;
    }

    /**
     * Convert JPA entity to domain aggregate
     * 
     * @param entity JPA entity
     * @return Domain aggregate
     */
    public Product toDomain(ProductJpaEntity entity) {
        // Reconstruct value objects
        ProductId id = ProductId.of(entity.getId());
        SKU sku = SKU.of(entity.getSku());
        Price price = Price.of(entity.getPrice(), Currency.getInstance(entity.getCurrency()));
        
        // Reconstruct reservations
        Map<String, Reservation> reservations = entity.getReservations()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Reservation.of(e.getKey(), e.getValue())
            ));
        
        Stock stock = Stock.of(
            entity.getTotalStock(),
            entity.getReservedStock(),
            reservations
        );
        
        // Reconstruct aggregate
        return Product.reconstitute(
            id,
            sku,
            entity.getName(),
            entity.getDescription(),
            price,
            stock,
            entity.isActive(),
            entity.isVisible(),
            entity.isAvailableForPurchase(),
            entity.isDiscontinued(),
            entity.getVersion() != null ? entity.getVersion().intValue() : 1,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
