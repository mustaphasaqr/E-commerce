package com.mustapha.ecommerce.product.application.facade;

import com.mustapha.ecommerce.product.application.command.*;
import com.mustapha.ecommerce.product.application.usecase.*;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.dto.ProductListResponse;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Facade - Translation Layer between API and Application
 * 
 * Responsibilities:
 * 1. Accept API DTOs (ProductRequest with primitives)
 * 2. Convert primitives → value objects → Commands
 * 3. Delegate to Use Cases (no business logic here)
 * 4. Convert Domain → API DTOs (ProductResponse)
 * 
 * What this is NOT:
 * - NOT a business logic layer (that's in Domain)
 * - NOT a transaction manager (that's in Use Cases)
 * - NOT an event publisher (that's in Use Cases)
 * - NOT a data access layer (that's in Repository)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 * Think: "Translator + Router"
 */
@Service
public class ProductFacade {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final GetProductBySkuUseCase getProductBySkuUseCase;
    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseReservationUseCase releaseReservationUseCase;
    private final FulfillReservationUseCase fulfillReservationUseCase;
    private final UpdatePriceUseCase updatePriceUseCase;
    private final UpdateProductDetailsUseCase updateProductDetailsUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final DiscontinueProductUseCase discontinueProductUseCase;
    private final com.mustapha.ecommerce.product.domain.repository.ProductRepository productRepository;

    public ProductFacade(CreateProductUseCase createProductUseCase,
                        GetProductByIdUseCase getProductByIdUseCase,
                        GetProductBySkuUseCase getProductBySkuUseCase,
                        ReserveStockUseCase reserveStockUseCase,
                        ReleaseReservationUseCase releaseReservationUseCase,
                        FulfillReservationUseCase fulfillReservationUseCase,
                        UpdatePriceUseCase updatePriceUseCase,
                        UpdateProductDetailsUseCase updateProductDetailsUseCase,
                        ActivateProductUseCase activateProductUseCase,
                        DeactivateProductUseCase deactivateProductUseCase,
                        DiscontinueProductUseCase discontinueProductUseCase,
                        com.mustapha.ecommerce.product.domain.repository.ProductRepository productRepository) {
        this.createProductUseCase = createProductUseCase;
        this.getProductByIdUseCase = getProductByIdUseCase;
        this.getProductBySkuUseCase = getProductBySkuUseCase;
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseReservationUseCase = releaseReservationUseCase;
        this.fulfillReservationUseCase = fulfillReservationUseCase;
        this.updatePriceUseCase = updatePriceUseCase;
        this.updateProductDetailsUseCase = updateProductDetailsUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
        this.discontinueProductUseCase = discontinueProductUseCase;
        this.productRepository = productRepository;
    }

    /**
     * Create Product - Entry point from REST API
     * 
     * Flow:
     * 1. Accept ProductRequest (API DTO with String/BigDecimal/int primitives)
     * 2. Convert to CreateProductCommand (with SKU, Price, Stock value objects)
     * 3. Call CreateProductUseCase (handles business orchestration + @Transactional)
     * 4. Return ProductResponse (API DTO)
     */
    public ProductResponse createProduct(ProductRequest request) {
        // Step 1: Convert API DTO → Application Command
        CreateProductCommand command = new CreateProductCommand(
            SKU.of(request.getSku()),
            request.getName(),
            request.getDescription(),
            Price.of(request.getPrice(), Currency.getInstance(request.getCurrencyCode())),
            Stock.of(request.getInitialStock())
        );
        
        // Step 2: Delegate to Use Case (handles @Transactional, events, business rules)
        Product product = createProductUseCase.execute(command);
        
        // Step 3: Convert Domain → API DTO
        return ProductResponse.fromDomain(product);
    }

    /**
     * Get Product by ID
     * 
     * Performance Optimization:
     * - Redis cache with 'sync = true' prevents cache stampede
     * - Only ONE thread queries DB when cache expires, others wait
     */
    @Cacheable(value = "products", key = "#productId", sync = true)
    public ProductResponse getProductById(String productId) {
        GetProductByIdQuery query = new GetProductByIdQuery(ProductId.of(productId));
        Product product = getProductByIdUseCase.execute(query);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Get Product by SKU
     * 
     * Performance Optimization:
     * - Redis cache with 'sync = true' prevents cache stampede
     * - Prevents thundering herd when cache expires under load
     */
    @Cacheable(value = "products", key = "#sku", sync = true)
    public ProductResponse getProductBySku(String sku) {
        GetProductBySkuQuery query = new GetProductBySkuQuery(sku);
        Product product = getProductBySkuUseCase.execute(query);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Reserve Stock
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse reserveStock(String productId, String orderId, int quantity) {
        ReserveStockCommand command = new ReserveStockCommand(
            ProductId.of(productId),
            orderId,
            quantity
        );
        Product product = reserveStockUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Release Reservation
     */
    public ProductResponse releaseReservation(String productId, String orderId) {
        ReleaseReservationCommand command = new ReleaseReservationCommand(
            ProductId.of(productId),
            orderId
        );
        Product product = releaseReservationUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Fulfill Reservation
     */
    public ProductResponse fulfillReservation(String productId, String orderId) {
        FulfillReservationCommand command = new FulfillReservationCommand(
            ProductId.of(productId),
            orderId
        );
        Product product = fulfillReservationUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Update Price
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse updatePrice(String productId, java.math.BigDecimal newPrice, String currencyCode) {
        UpdatePriceCommand command = new UpdatePriceCommand(
            ProductId.of(productId),
            Price.of(newPrice, Currency.getInstance(currencyCode))
        );
        Product product = updatePriceUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Update Product Details
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse updateProductDetails(String productId, String name, String description) {
        UpdateProductDetailsCommand command = new UpdateProductDetailsCommand(
            ProductId.of(productId),
            name,
            description
        );
        Product product = updateProductDetailsUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Activate Product
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse activateProduct(String productId) {
        ActivateProductCommand command = new ActivateProductCommand(ProductId.of(productId));
        Product product = activateProductUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Deactivate Product
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse deactivateProduct(String productId) {
        DeactivateProductCommand command = new DeactivateProductCommand(ProductId.of(productId));
        Product product = deactivateProductUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * Discontinue Product
     */
    @CacheEvict(value = "products", key = "#productId")
    public ProductResponse discontinueProduct(String productId) {
        DiscontinueProductCommand command = new DiscontinueProductCommand(ProductId.of(productId));
        Product product = discontinueProductUseCase.execute(command);
        return ProductResponse.fromDomain(product);
    }

    /**
     * List All Products - Lightweight DTO for list view
     * Performance: 46% smaller payload than full ProductResponse
     * Use for: Product catalog, search results, browsing
     */
    public List<ProductListResponse> listProducts() {
        return productRepository.findAll().stream()
            .map(ProductListResponse::fromDomain)
            .collect(Collectors.toList());
    }
}
