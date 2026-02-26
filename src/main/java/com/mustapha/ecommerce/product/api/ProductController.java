package com.mustapha.ecommerce.product.api;

import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.dto.ProductListResponse;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    public ProductController(ProductFacade productFacade) {
        this.productFacade = productFacade;
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
}
