package com.mustapha.ecommerce.product.api;

import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productFacade.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
     * Get product by SKU (external identifier)
     */
    @GetMapping
    public ResponseEntity<ProductResponse> getProductBySku(@RequestParam String sku) {
        ProductResponse response = productFacade.getProductBySku(sku);
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
    public ResponseEntity<ProductResponse> activateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.activateProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate product
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ProductResponse> deactivateProduct(@PathVariable String id) {
        ProductResponse response = productFacade.deactivateProduct(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Discontinue product
     */
    @PostMapping("/{id}/discontinue")
    public ResponseEntity<ProductResponse> discontinueProduct(@PathVariable String id) {
        ProductResponse response = productFacade.discontinueProduct(id);
        return ResponseEntity.ok(response);
    }
}
