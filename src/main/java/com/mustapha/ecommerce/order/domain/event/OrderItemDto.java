package com.mustapha.ecommerce.order.domain.event;

/**
 * Order Item DTO for Domain Events
 * 
 * Responsibility: Lightweight representation of order item for events
 * Pattern: DTO (Data Transfer Object) for event payloads
 * 
 * Use Case: Include order item details in events without coupling to full OrderItem entity
 */
public record OrderItemDto(
    String productId,
    int quantity
) {
    public OrderItemDto {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + quantity);
        }
    }
}
