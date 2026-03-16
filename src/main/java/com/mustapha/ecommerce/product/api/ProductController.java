package com.mustapha.ecommerce.product.api;

import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.application.port.RecommendationPort;
import com.mustapha.ecommerce.product.application.port.RecommendationPort.ProductRecommendation;
import com.mustapha.ecommerce.product.application.port.ProductReviewPort;
import com.mustapha.ecommerce.product.application.port.ProductReviewPort.*;
import com.mustapha.ecommerce.product.dto.ProductListResponse;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP Boundary - Product Controller
 * Responsibility: Request/Response mapping, Syntactic validation, HTTP error translation
 * Pattern: Facade (Controller → Application Facade)
 * SOLID: SRP (HTTP only), DIP (depends on application interfaces)
 */
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Comprehensive product catalog management including CRUD operations, inventory management, image uploads, reviews, and recommendations")
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

    @Operation(
        summary = "Create Product",
        description = """
            Create a new product in the catalog. Requires EMPLOYEE or OWNER role.
            
            **Features:**
            - Validates product data
            - Generates unique SKU
            - XSS protection on name/description
            - Sets initial stock levels
            - Creates product status as ACTIVE
            
            **Security:** Requires EMPLOYEE or OWNER role
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Product Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation error or HTML tags detected",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions (requires EMPLOYEE or OWNER)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Product details to create", required = true)
            @Valid @RequestBody ProductRequest request) {
        // XSS prevention - reject HTML tags in product name
        if (request.getName() != null && (request.getName().contains("<") || request.getName().contains(">"))) {
            throw new IllegalArgumentException("Product name cannot contain HTML tags");
        }
        ProductResponse response = productFacade.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "List Products or Get by SKU",
        description = """
            List all products or get specific product by SKU parameter.
            
            **Modes:**
            - No parameters: Returns all products (lightweight ProductListResponse - 46% smaller)
            - With SKU: Returns full product details by SKU
            
            **Performance:** Lightweight list response for better performance
            
            **Security:** Public endpoint
            """,
        tags = {"Product Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Success - Returns product list or single product",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found - Product with specified SKU not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<?> listProducts(
            @Parameter(description = "Optional SKU to get specific product")
            @RequestParam(required = false) String sku) {
        if (sku != null && !sku.isBlank()) {
            // Get product by SKU - full details
            ProductResponse response = productFacade.getProductBySku(sku);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        // List all products - lightweight DTOs
        List<ProductListResponse> products = productFacade.listProducts();
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Search products by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductListResponse>> searchProducts(@RequestParam(required = false) String name) {
        List<ProductListResponse> products = productFacade.listProducts();

        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.OK).body(products);
        }

        String term = name.trim().toLowerCase(Locale.ROOT);
        List<ProductListResponse> filtered = products.stream()
                .filter(product -> (product.getName() != null && product.getName().toLowerCase(Locale.ROOT).contains(term))
                        || (product.getSku() != null && product.getSku().toLowerCase(Locale.ROOT).contains(term)))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(filtered);
    }

    @Operation(
        summary = "Get Product by ID",
        description = """
            Retrieve complete product details by internal product ID.
            
            **Returns:**
            - Full product information
            - Current stock levels
            - Price and currency
            - Product status
            - Image URLs
            
            **Security:** Public endpoint
            """,
        tags = {"Product Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found with specified ID",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product internal ID", required = true, example = "PROD-123456")
            @PathVariable String id) {
        ProductResponse response = productFacade.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Release stock reservation (order cancelled)
     */
    @PostMapping("/{id}/release-reservation")
    public ResponseEntity<ProductResponse> releaseReservation(
            @PathVariable String id,
            @RequestParam String orderId) {
        ProductResponse response = productFacade.releaseReservation(id, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Fulfill stock reservation (order shipped)
     */
    @PostMapping("/{id}/fulfill-reservation")
    public ResponseEntity<ProductResponse> fulfillReservation(
            @PathVariable String id,
            @RequestParam String orderId) {
        ProductResponse response = productFacade.fulfillReservation(id, orderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Update Product Price",
        description = """
            Update product price and currency. Requires EMPLOYEE or OWNER role.
            
            **Features:**
            - Validates price (must be positive)
            - Supports multi-currency
            - Audit trail for price changes
            
            **Security:** Requires EMPLOYEE or OWNER role
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Product Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Price updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid price or currency",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> updatePrice(
            @Parameter(description = "Product ID", required = true, example = "PROD-123456")
            @PathVariable String id,
            @Parameter(description = "New price (must be positive)", required = true, example = "29.99")
            @RequestParam java.math.BigDecimal newPrice,
            @Parameter(description = "Currency code (ISO 4217)", required = true, example = "USD")
            @RequestParam String currencyCode) {
        ProductResponse response = productFacade.updatePrice(id, newPrice, currencyCode);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Activate product
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.activateProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deactivate product
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> deactivateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.deactivateProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Discontinue product
     */
    @PostMapping("/{id}/discontinue")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<ProductResponse> discontinueProduct(@PathVariable String id) {
        ProductResponse response = productFacade.discontinueProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @Operation(
        summary = "Upload Product Image",
        description = """
            Upload product image (JPEG, PNG, GIF, WebP). Requires EMPLOYEE or OWNER role.
            
            **Features:**
            - Supported formats: JPEG, PNG, GIF, WebP
            - Max file size: 5 MB
            - Automatic image optimization
            - Generates CDN URL
            - XSS protection on filenames
            
            **Security:** Requires EMPLOYEE or OWNER role
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Product Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Image uploaded successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "productId": "PROD-123456",
                          "imageUrl": "https://cdn.example.com/products/prod-123456/image1.jpg",
                          "message": "Image uploaded successfully"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid file type or empty file",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\": \"Only image files are allowed\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Insufficient permissions",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Payload Too Large - File exceeds 5 MB limit",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @Parameter(description = "Product ID", required = true, example = "PROD-123456")
            @PathVariable String id,
            @Parameter(description = "Image file (JPEG/PNG/GIF/WebP, max 5MB)", required = true)
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
        
        String imageUrl = productFacade.uploadProductImage(id, file);

        Map<String, Object> response = new HashMap<>();
        response.put("productId", id);
        response.put("imageUrl", imageUrl);
        response.put("message", "Image uploaded successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Delete product image
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'OWNER')")
    public ResponseEntity<Map<String, String>> deleteImage(
            @PathVariable String id,
            @RequestParam("imageUrl") String imageUrl) {

        productFacade.deleteProductImage(id, imageUrl);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of(
            "productId", id,
            "message", "Image deleted successfully"
        ));
    }

    // ========== REVIEW ENDPOINTS ==========

    /**
     * Get product reviews (paginated)
     * GET /api/products/{id}/reviews?page=0&size=10&sortBy=MOST_HELPFUL
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<ReviewsPage> getProductReviews(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "MOST_HELPFUL") SortBy sortBy) {
        
        ReviewsPage reviews = productReviewPort.getProductReviews(id, page, size, sortBy);
        return ResponseEntity.status(HttpStatus.OK).body(reviews);
    }

    /**
     * Get product review statistics
     * GET /api/products/{id}/reviews/stats
     */
    @GetMapping("/{id}/reviews/stats")
    public ResponseEntity<ProductReviewStats> getProductReviewStats(@PathVariable String id) {
        ProductReviewStats stats = productReviewPort.getProductReviewStats(id);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    /**
     * Submit a product review
     * POST /api/products/{id}/reviews
     * Requires authentication
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<Map<String, Object>> submitReview(
            @PathVariable String id,
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
            @PathVariable String productId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String userId) {
        
        productReviewPort.markHelpful(reviewId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Review marked as helpful"));
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
        return ResponseEntity.status(HttpStatus.OK).body(recommendations);
    }

    /**
     * Get personalized recommendations for a customer
     * GET /api/products/recommendations/for-you?limit=10
     */
    @GetMapping("/recommendations/for-you")
    public ResponseEntity<List<ProductRecommendation>> getPersonalizedRecommendations(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "10") int limit) {

        if (userId == null || userId.isBlank()) {
            return ResponseEntity.status(HttpStatus.OK).body(java.util.Collections.emptyList());
        }
        
        List<ProductRecommendation> recommendations = 
            recommendationPort.getRecommendationsForCustomer(userId, limit);
        return ResponseEntity.status(HttpStatus.OK).body(recommendations);
    }

    /**
     * Get frequently bought together products
     * GET /api/products/{id}/recommendations/frequently-bought-together
     */
    @GetMapping("/{id}/recommendations/frequently-bought-together")
    public ResponseEntity<List<ProductRecommendation>> getFrequentlyBoughtTogether(
            @PathVariable String id,
            @RequestParam(defaultValue = "5") int limit) {
        
        List<ProductRecommendation> recommendations = 
            recommendationPort.getFrequentlyBoughtTogether(id, limit);
        return ResponseEntity.status(HttpStatus.OK).body(recommendations);
    }
}
