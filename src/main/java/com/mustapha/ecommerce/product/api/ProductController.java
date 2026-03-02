package com.mustapha.ecommerce.product.api;

import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.application.port.RecommendationPort;
import com.mustapha.ecommerce.product.application.port.RecommendationPort.ProductRecommendation;
import com.mustapha.ecommerce.product.application.port.ProductReviewPort;
import com.mustapha.ecommerce.product.application.port.ProductReviewPort.*;
import com.mustapha.ecommerce.product.dto.ProductListResponse;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Boundary - Product Controller
 * Responsibility: Request/Response mapping, Syntactic validation, HTTP error translation
 * Pattern: Facade (Controller → Application Facade)
 * SOLID: SRP (HTTP only), DIP (depends on application interfaces)
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductFacade productFacade;
    private final ProductReviewPort productReviewPort;
    private final RecommendationPort recommendationPort;

    public ProductController(ProductFacade productFacade, 
                           ProductReviewPort productReviewPort,
                           RecommendationPort recommendationPort) {
        this.productFacade = productFacade;
        this.productReviewPort = productReviewPort;
        this.recommendationPort = recommendationPort;
    }

    /**
     * Create new product
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        // XSS prevention - reject HTML tags in product name
        if (request.getName() != null && (request.getName().contains("<") || request.getName().contains(">"))) {
            throw new IllegalArgumentException("Product name cannot contain HTML tags");
        }
        ProductResponse response = productFacade.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all products or get product by SKU
     * Performance: Returns lightweight ProductListResponse (46% smaller than ProductResponse)
     */
    @GetMapping
    public ResponseEntity<?> listProducts(@RequestParam(required = false) String sku) {
        if (sku != null && !sku.isBlank()) {
            // Get product by SKU - full details
            ProductResponse response = productFacade.getProductBySku(sku);
            return ResponseEntity.ok(response);
        }
        // List all products - lightweight DTOs
        List<ProductListResponse> products = productFacade.listProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Search products by name
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam(required = false) String name) {
        // For now, return empty list (would need search use case)
        return ResponseEntity.ok(java.util.Collections.emptyList());
    }

    /**
     * Get product by internal ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse response = productFacade.getProductById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Reserve stock for an order (called by Order service)
     */
    @PostMapping("/{id}/reserve-stock")
    public ResponseEntity<ProductResponse> reserveStock(
            @PathVariable String id,
            @RequestParam String orderId,
            @RequestParam int quantity) {
        ProductResponse response = productFacade.reserveStock(id, orderId, quantity);
        return ResponseEntity.ok(response);
    }

    /**
     * Release stock reservation (order cancelled)
     */
    @PostMapping("/{id}/release-reservation")
    public ResponseEntity<ProductResponse> releaseReservation(
            @PathVariable String id,
            @RequestParam String orderId) {
        ProductResponse response = productFacade.releaseReservation(id, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fulfill stock reservation (order shipped)
     */
    @PostMapping("/{id}/fulfill-reservation")
    public ResponseEntity<ProductResponse> fulfillReservation(
            @PathVariable String id,
            @RequestParam String orderId) {
        ProductResponse response = productFacade.fulfillReservation(id, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update product price
     */
    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> updatePrice(
            @PathVariable String id,
            @RequestParam java.math.BigDecimal newPrice,
            @RequestParam String currencyCode) {
        ProductResponse response = productFacade.updatePrice(id, newPrice, currencyCode);
        return ResponseEntity.ok(response);
    }

    /**
     * Update product details
     */
    @PutMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> updateProductDetails(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String description) {
        ProductResponse response = productFacade.updateProductDetails(id, name, description);
        return ResponseEntity.ok(response);
    }

    /**
     * Activate product
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.activateProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate product
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> deactivateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.deactivateProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Discontinue product
     */
    @PostMapping("/{id}/discontinue")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> discontinueProduct(@PathVariable String id) {
        ProductResponse response = productFacade.discontinueProduct(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Upload product image
     * Accepts: image/jpeg, image/png, image/gif, image/webp
     * Max size: 5 MB (configured in storage service)
     */
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
        }
        
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only image files are allowed"));
        }
        
        try {
            String imageUrl = productFacade.uploadProductImage(id, file);
            
            Map<String, Object> response = new HashMap<>();
            response.put("productId", id);
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }
    
    /**
     * Delete product image
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable String id,
            @RequestParam("imageUrl") String imageUrl) {
        
        try {
            productFacade.deleteProductImage(id, imageUrl);
            
            return ResponseEntity.ok(Map.of(
                    "productId", id,
                    "message", "Image deleted successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete image: " + e.getMessage()));
        }
    }

    // ========== REVIEW ENDPOINTS ==========

    /**
     * Get product reviews (paginated)
     * GET /api/products/{id}/reviews?page=0&size=10&sortBy=MOST_HELPFUL
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewsPage> getProductReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "MOST_HELPFUL") SortBy sortBy) {
        
        ReviewsPage reviews = productReviewPort.getProductReviews(id, page, size, sortBy);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get product review statistics
     * GET /api/products/{id}/reviews/stats
     */
    @GetMapping("/{id}/reviews/stats")
    public ResponseEntity<ProductReviewStats> getProductReviewStats(@PathVariable Long id) {
        ProductReviewStats stats = productReviewPort.getProductReviewStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Submit a product review
     * POST /api/products/{id}/reviews
     * Requires authentication
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(
            @PathVariable Long id,
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody SubmitReviewRequest request) {
        
        Long reviewId = productReviewPort.submitReview(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("reviewId", reviewId, "message", "Review submitted successfully"));
    }

    /**
     * Mark review as helpful
     * POST /api/products/{productId}/reviews/{reviewId}/helpful
     */
    @PostMapping("/{productId}/reviews/{reviewId}/helpful")
    public ResponseEntity<Map<String, String>> markReviewHelpful(
            @PathVariable Long productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String userId) {
        
        productReviewPort.markHelpful(reviewId, Long.parseLong(userId));
        return ResponseEntity.ok(Map.of("message", "Review marked as helpful"));
    }

    // ========== RECOMMENDATION ENDPOINTS ==========

    /**
     * Get trending product recommendations
     * GET /api/products/recommendations/trending?limit=10
     */
    @GetMapping("/recommendations/trending")
    public ResponseEntity<List<ProductRecommendation>> getTrendingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductRecommendation> recommendations = recommendationPort.getTrendingProducts(limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get personalized recommendations for a customer
     * GET /api/products/recommendations/for-you?limit=10
     */
    @GetMapping("/recommendations/for-you")
    public ResponseEntity<List<ProductRecommendation>> getPersonalizedRecommendations(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ProductRecommendation> recommendations = 
            recommendationPort.getRecommendationsForCustomer(Long.parseLong(userId), limit);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get frequently bought together products
     * GET /api/products/{id}/recommendations/frequently-bought-together
     */
    @GetMapping("/{id}/recommendations/frequently-bought-together")
    public ResponseEntity<List<ProductRecommendation>> getFrequentlyBoughtTogether(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<ProductRecommendation> recommendations = 
            recommendationPort.getFrequentlyBoughtTogether(id, limit);
        return ResponseEntity.ok(recommendations);
    }
}
