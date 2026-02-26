package com.mustapha.ecommerce.shared.security.authorization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatic resource ownership verification using AOP.
 * 
 * <p>Place this annotation on controller or service methods that require ownership checks.
 * The aspect will automatically extract the authenticated user ID and resource ID,
 * then verify ownership before allowing method execution.
 * 
 * <p>Usage Example:
 * <pre>{@code
 * @DeleteMapping("/orders/{orderId}")
 * @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
 * public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
 *     // Only the order owner can delete their order
 *     orderService.deleteOrder(orderId);
 *     return ResponseEntity.noContent().build();
 * }
 * 
 * @PutMapping("/posts/{postId}")
 * @VerifyOwnership(resourceType = ResourceType.POST, resourceIdParam = "postId")
 * public ResponseEntity<PostResponse> updatePost(
 *         @PathVariable String postId,
 *         @RequestBody UpdatePostRequest request) {
 *     // Only the post author can update their post
 *     return ResponseEntity.ok(postService.updatePost(postId, request));
 * }
 * }</pre>
 * 
 * <p>How It Works:
 * <ol>
 *   <li>OwnershipAspect intercepts methods annotated with @VerifyOwnership</li>
 *   <li>Extracts authenticated userId from SecurityContext</li>
 *   <li>Extracts resourceId from method parameters using resourceIdParam</li>
 *   <li>Calls ResourceOwnershipService to verify ownership</li>
 *   <li>Throws ForbiddenException if not owner, otherwise allows method execution</li>
 * </ol>
 * 
 * <p>Benefits:
 * <ul>
 *   <li>Declarative authorization - clear intent in code</li>
 *   <li>Eliminates repetitive ownership check boilerplate</li>
 *   <li>Centralized ownership logic in ResourceOwnershipService</li>
 *   <li>Easy to audit (search for @VerifyOwnership)</li>
 *   <li>Type-safe with ResourceType enum</li>
 * </ul>
 * 
 * @see ResourceOwnershipService
 * @see OwnershipAspect
 * @see ResourceType
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifyOwnership {
    
    /**
     * The type of resource to verify ownership for.
     * 
     * <p>This determines which repository/service the aspect will use for ownership lookup.
     * Each ResourceType corresponds to a different ownership verification strategy.
     * 
     * @return the resource type (e.g., ORDER, POST, COMMENT)
     */
    ResourceType resourceType();
    
    /**
     * The name of the method parameter that contains the resource ID.
     * 
     * <p>The aspect will extract this parameter's value and use it as the resourceId
     * for ownership verification. Parameter name must match exactly.
     * 
     * <p>Examples:
     * <ul>
     *   <li>"orderId" - extracts value from @PathVariable String orderId</li>
     *   <li>"postId" - extracts value from @PathVariable String postId</li>
     *   <li>"commentId" - extracts value from @RequestBody or @PathVariable</li>
     * </ul>
     * 
     * @return the parameter name containing the resource ID
     */
    String resourceIdParam();
    
    /**
     * Optional custom error message for authorization failures.
     * 
     * <p>If provided, this message will be used instead of the default
     * "You do not have permission to access this resource" message.
     * 
     * @return custom error message, or empty string for default
     */
    String errorMessage() default "";
}
