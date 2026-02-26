package com.mustapha.ecommerce.shared.security.authorization;

/**
 * Enumeration of resource types that support ownership verification.
 * 
 * <p>Each type represents a domain entity that has an owner (typically the user who created it).
 * The ResourceOwnershipService uses this enum to determine which repository/service
 * to query for ownership verification.
 * 
 * <p>Usage:
 * {@code @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")}
 * 
 * <p>When adding new resource types:
 * <ol>
 *   <li>Add enum constant here</li>
 *   <li>Update ResourceOwnershipService.isOwner() with ownership verification logic</li>
 *   <li>Ensure the entity has userId/ownerId field for lookup</li>
 * </ol>
 */
public enum ResourceType {
    
    /**
     * Order resource.
     * Ownership determined by Order.userId field.
     */
    ORDER,
    
    /**
     * Product resource (for sellers).
     * Ownership determined by Product.sellerId field.
     */
    PRODUCT,
    
    /**
     * Address resource.
     * Ownership determined by Address.userId field.
     */
    ADDRESS,
    
    /**
     * Shopping cart resource.
     * Ownership determined by Cart.userId field.
     */
    CART,
    
    /**
     * Review resource.
     * Ownership determined by Review.userId field.
     */
    REVIEW,
    
    /**
     * Wishlist resource.
     * Ownership determined by Wishlist.userId field.
     */
    WISHLIST,
    
    /**
     * User profile resource.
     * Ownership determined by User.id field (self-ownership).
     */
    USER_PROFILE
}
