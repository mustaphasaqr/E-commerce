package com.mustapha.ecommerce.shared.security.authorization;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for verifying resource ownership before allowing operations.
 * 
 * <p>This service centralizes authorization logic by checking if a user owns a specific resource.
 * Used by OwnershipAspect to automatically verify ownership for methods annotated with @VerifyOwnership.
 * 
 * <p>Why Centralized Ownership Service?
 * <ul>
 *   <li>Single Responsibility: All ownership logic in one place</li>
 *   <li>Consistency: Same ownership rules across all endpoints</li>
 *   <li>Auditing: Easy to track authorization decisions</li>
 *   <li>Testing: Centralized logic is easier to unit test</li>
 *   <li>Performance: Can add caching layer for frequently checked resources</li>
 * </ul>
 * 
 * <p>Ownership Rules by Resource Type:
 * <ul>
 *   <li>ORDER: User must be the customerId on the order</li>
 *   <li>PRODUCT: User must be the sellerId on the product (future)</li>
 *   <li>ADDRESS: User must be the owner of the address (future)</li>
 *   <li>CART: User must be the owner of the cart (future)</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Direct usage in service layer:
 * ownershipService.checkOwnership(userId, orderId, ResourceType.ORDER);
 * 
 * // Automatic usage via annotation:
 * @VerifyOwnership(resourceType = ORDER, resourceIdParam = "orderId")
 * public void deleteOrder(String orderId) { ... }
 * }</pre>
 */
@Service
public class ResourceOwnershipService {
    
    private static final Logger logger = LoggerFactory.getLogger(ResourceOwnershipService.class);
    
    private final OrderRepository orderRepository;
    
    public ResourceOwnershipService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    /**
     * Checks if a user owns the specified resource and throws ForbiddenException if not.
     * 
     * <p>This is the main method used by OwnershipAspect for automatic authorization.
     * It logs all ownership verification attempts for security auditing.
     * 
     * @param userId the authenticated user's ID
     * @param resourceId the resource ID to check (as String from path variable)
     * @param resourceType the type of resource being accessed
     * @throws ForbiddenException if user does not own the resource
     */
    public void checkOwnership(String userId, String resourceId, ResourceType resourceType) {
        logger.debug("Checking ownership: userId={}, resourceId={}, resourceType={}", 
                     userId, resourceId, resourceType);
        
        boolean isOwner = isOwner(userId, resourceId, resourceType);
        
        if (!isOwner) {
            logger.warn("Ownership verification failed: userId={}, resourceId={}, resourceType={}", 
                        userId, resourceId, resourceType);
            throw new ForbiddenException(
                ErrorCode.AUTHZ_NOT_RESOURCE_OWNER,
                "You do not have permission to access this " + resourceType.name().toLowerCase()
            );
        }
        
        logger.debug("Ownership verified successfully: userId={}, resourceId={}, resourceType={}", 
                     userId, resourceId, resourceType);
    }
    
    /**
     * Checks if a user owns the specified resource (boolean return).
     * 
     * <p>Use this method when you need conditional logic based on ownership
     * rather than throwing an exception.
     * 
     * @param userId the authenticated user's ID
     * @param resourceId the resource ID to check (as String from path variable)
     * @param resourceType the type of resource being accessed
     * @return true if user owns the resource, false otherwise
     */
    public boolean isOwner(String userId, String resourceId, ResourceType resourceType) {
        return switch (resourceType) {
            case ORDER -> isOrderOwner(userId, resourceId);
            case PRODUCT -> isProductOwner(userId, resourceId);
            case ADDRESS -> isAddressOwner(userId, resourceId);
            case CART -> isCartOwner(userId, resourceId);
            case REVIEW -> isReviewOwner(userId, resourceId);
            case WISHLIST -> isWishlistOwner(userId, resourceId);
            case USER_PROFILE -> isUserProfileOwner(userId, resourceId);
        };
    }
    
    /**
     * Checks if user owns the specified order.
     * 
     * <p>Implementation:
     * <ol>
     *   <li>Convert String orderId to OrderId value object</li>
     *   <li>Query OrderRepository to find the order</li>
     *   <li>If order not found, return true (let use case throw 404)</li>
     *   <li>Compare order's customerId with authenticated userId</li>
     * </ol>
     * 
     * <p>NOTE: Returns true when order doesn't exist to allow the use case to throw
     * proper OrderNotFoundException (404). Authorization should only deny access to 
     * EXISTING resources the user doesn't own (returning 403 for non-existent resources
     * would leak information about which resource IDs exist in the system).
     * 
     * @param userId the authenticated user's ID (String from JWT)
     * @param orderId the order ID (String from path variable)
     * @return true if order not found OR user is owner, false if order exists but different owner
     */
    private boolean isOrderOwner(String userId, String orderId) {
        try {
            // Convert String IDs to value objects for domain repository
            OrderId orderIdVO = new OrderId(orderId);
            CustomerId customerIdVO = new CustomerId(userId);
            
            // Find order in repository
            return orderRepository.findById(orderIdVO)
                    .map(order -> order.getCustomerId().equals(customerIdVO))
                    .orElse(true); // Order not found = allow (use case will throw 404)
            
        } catch (IllegalArgumentException e) {
            // Invalid ID format (malformed UUID, etc.) - deny access
            logger.warn("Invalid order ID format: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Checks if user owns the specified product (seller ownership).
     * 
     * <p>TODO: Implement when Product entity includes sellerId field.
     * Currently returns false to deny access safely.
     * 
     * @param userId the authenticated user's ID
     * @param productId the product ID
     * @return true if user is the product's seller, false otherwise
     */
    private boolean isProductOwner(String userId, String productId) {
        // TODO: Implement product ownership check
        // Requires Product entity to have sellerId field
        logger.warn("Product ownership check not yet implemented - denying access");
        return false;
    }
    
    /**
     * Checks if user owns the specified address.
     * 
     * <p>TODO: Implement when Address entity is created with userId field.
     * Currently returns false to deny access safely.
     * 
     * @param userId the authenticated user's ID
     * @param addressId the address ID
     * @return true if user owns the address, false otherwise
     */
    private boolean isAddressOwner(String userId, String addressId) {
        // TODO: Implement address ownership check
        // Requires Address entity with userId field
        logger.warn("Address ownership check not yet implemented - denying access");
        return false;
    }
    
    /**
     * Checks if user owns the specified cart.
     * 
     * <p>TODO: Implement when Cart entity is created with userId field.
     * Currently returns false to deny access safely.
     * 
     * @param userId the authenticated user's ID
     * @param cartId the cart ID
     * @return true if user owns the cart, false otherwise
     */
    private boolean isCartOwner(String userId, String cartId) {
        // TODO: Implement cart ownership check
        // Requires Cart entity with userId field
        logger.warn("Cart ownership check not yet implemented - denying access");
        return false;
    }
    
    /**
     * Checks if user owns the specified review.
     * 
     * <p>TODO: Implement when Review entity is created with userId field.
     * Currently returns false to deny access safely.
     * 
     * @param userId the authenticated user's ID
     * @param reviewId the review ID
     * @return true if user owns the review, false otherwise
     */
    private boolean isReviewOwner(String userId, String reviewId) {
        // TODO: Implement review ownership check
        // Requires Review entity with userId field
        logger.warn("Review ownership check not yet implemented - denying access");
        return false;
    }
    
    /**
     * Checks if user owns the specified wishlist.
     * 
     * <p>TODO: Implement when Wishlist entity is created with userId field.
     * Currently returns false to deny access safely.
     * 
     * @param userId the authenticated user's ID
     * @param wishlistId the wishlist ID
     * @return true if user owns the wishlist, false otherwise
     */
    private boolean isWishlistOwner(String userId, String wishlistId) {
        // TODO: Implement wishlist ownership check
        // Requires Wishlist entity with userId field
        logger.warn("Wishlist ownership check not yet implemented - denying access");
        return false;
    }
    
    /**
     * Checks if userId matches the user profile ID (self-ownership).
     * 
     * <p>This is the simplest ownership check - users can only access their own profiles.
     * No repository query needed since we just compare IDs.
     * 
     * @param userId the authenticated user's ID
     * @param profileUserId the user profile ID being accessed
     * @return true if IDs match (user accessing own profile), false otherwise
     */
    private boolean isUserProfileOwner(String userId, String profileUserId) {
        return userId.equals(profileUserId);
    }
}
