package com.mustapha.ecommerce.product.application.facade;

import com.mustapha.ecommerce.product.application.command.*;
import com.mustapha.ecommerce.product.application.usecase.*;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.*;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductFacade Tests - Translation & Orchestration Layer
 * 
 * Tests verify:
 * 1. DTO → Command/Query conversion (primitives → value objects)
 * 2. Use case delegation (correct use case called)
 * 3. Domain → DTO conversion (value objects → primitives)
 * 4. No business logic in facade (pure translation + delegation)
 * 
 * Mocking strategy:
 * - Mock all use cases (already tested in Phase 2)
 * - Verify command/query creation
 * - Verify correct use case called
 * - Verify ProductResponse.fromDomain() conversion
 */
class ProductFacadeTest {

    @Mock
    private CreateProductUseCase createProductUseCase;
    
    @Mock
    private GetProductByIdUseCase getProductByIdUseCase;
    
    @Mock
    private GetProductBySkuUseCase getProductBySkuUseCase;
    
    @Mock
    private ReserveStockUseCase reserveStockUseCase;
    
    @Mock
    private ReleaseReservationUseCase releaseReservationUseCase;
    
    @Mock
    private FulfillReservationUseCase fulfillReservationUseCase;
    
    @Mock
    private UpdatePriceUseCase updatePriceUseCase;
    
    @Mock
    private UpdateProductDetailsUseCase updateProductDetailsUseCase;
    
    @Mock
    private ActivateProductUseCase activateProductUseCase;
    
    @Mock
    private DeactivateProductUseCase deactivateProductUseCase;
    
    @Mock
    private DiscontinueProductUseCase discontinueProductUseCase;
    
    @Mock
    private com.mustapha.ecommerce.product.domain.repository.ProductRepository productRepository;
    
    @InjectMocks
    private ProductFacade productFacade;
    
    private Product testProduct;
    private ProductId testProductId;
    private SKU testSku;
    private Price testPrice;
    private Stock testStock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create test product for use cases to return
        testSku = SKU.of("TEST-SKU");
        testPrice = Price.of(new BigDecimal("99.99"), Currency.getInstance("USD"));
        testStock = Stock.of(100);
        testProduct = Product.create(testSku, "Test Product", "Test Description", testPrice, testStock);
        testProductId = testProduct.getId();
        
        // Clear creation events
        testProduct.clearDomainEvents();
    }

    // ========== CREATE PRODUCT TESTS ==========

    @Test
    void createProduct_shouldConvertRequestToCommandAndCallUseCase() {
        // Given
        ProductRequest request = new ProductRequest(
            "TEST-SKU",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100
        );
        
        when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.createProduct(request);
        
        // Then - Verify command creation
        ArgumentCaptor<CreateProductCommand> commandCaptor = ArgumentCaptor.forClass(CreateProductCommand.class);
        verify(createProductUseCase).execute(commandCaptor.capture());
        
        CreateProductCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getSku().getValue()).isEqualTo("TEST-SKU");
        assertThat(capturedCommand.getName()).isEqualTo("Test Product");
        assertThat(capturedCommand.getDescription()).isEqualTo("Test Description");
        assertThat(capturedCommand.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(capturedCommand.getPrice().getCurrency()).isEqualTo(Currency.getInstance("USD"));
        assertThat(capturedCommand.getStock().getQuantity()).isEqualTo(100);
        
        // Then - Verify response conversion
        assertThat(response.getId()).isEqualTo(testProduct.getId().getValue());
        assertThat(response.getSku()).isEqualTo("TEST-SKU");
        assertThat(response.getName()).isEqualTo("Test Product");
        assertThat(response.getDescription()).isEqualTo("Test Description");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(response.getCurrency()).isEqualTo("USD");
        assertThat(response.getTotalStock()).isEqualTo(100);
    }

    @Test
    void createProduct_shouldHandleNullDescription() {
        // Given
        ProductRequest request = new ProductRequest(
            "TEST-SKU",
            "Test Product",
            null,  // No description
            new BigDecimal("99.99"),
            "USD",
            100
        );
        
        Product productWithoutDescription = Product.create(
            testSku,
            "Test Product",
            null,
            testPrice,
            testStock
        );
        
        when(createProductUseCase.execute(any(CreateProductCommand.class))).thenReturn(productWithoutDescription);
        
        // When
        ProductResponse response = productFacade.createProduct(request);
        
        // Then
        assertThat(response.getDescription()).isNull();
        verify(createProductUseCase).execute(any(CreateProductCommand.class));
    }

    // ========== GET PRODUCT BY ID TESTS ==========

    @Test
    void getProductById_shouldConvertStringIdToProductIdAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        when(getProductByIdUseCase.execute(any(GetProductByIdQuery.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.getProductById(productIdString);
        
        // Then - Verify query creation
        ArgumentCaptor<GetProductByIdQuery> queryCaptor = ArgumentCaptor.forClass(GetProductByIdQuery.class);
        verify(getProductByIdUseCase).execute(queryCaptor.capture());
        
        GetProductByIdQuery capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery.getProductId().getValue()).isEqualTo(productIdString);
        
        // Then - Verify response
        assertThat(response.getId()).isEqualTo(productIdString);
        assertThat(response.getSku()).isEqualTo("TEST-SKU");
    }

    // ========== GET PRODUCT BY SKU TESTS ==========

    @Test
    void getProductBySku_shouldConvertStringSkuToQueryAndCallUseCase() {
        // Given
        String skuString = "TEST-SKU";
        when(getProductBySkuUseCase.execute(any(GetProductBySkuQuery.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.getProductBySku(skuString);
        
        // Then - Verify query creation
        ArgumentCaptor<GetProductBySkuQuery> queryCaptor = ArgumentCaptor.forClass(GetProductBySkuQuery.class);
        verify(getProductBySkuUseCase).execute(queryCaptor.capture());
        
        GetProductBySkuQuery capturedQuery = queryCaptor.getValue();
        assertThat(capturedQuery.sku()).isEqualTo(skuString);
        
        // Then - Verify response
        assertThat(response.getSku()).isEqualTo(skuString);
    }

    // ========== RESERVE STOCK TESTS ==========

    @Test
    void reserveStock_shouldConvertParametersToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        String orderId = "ORDER-001";
        int quantity = 10;
        
        testProduct.reserveStockForOrder(orderId, quantity);
        testProduct.clearDomainEvents();
        
        when(reserveStockUseCase.execute(any(ReserveStockCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.reserveStock(productIdString, orderId, quantity);
        
        // Then - Verify command creation
        ArgumentCaptor<ReserveStockCommand> commandCaptor = ArgumentCaptor.forClass(ReserveStockCommand.class);
        verify(reserveStockUseCase).execute(commandCaptor.capture());
        
        ReserveStockCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        assertThat(capturedCommand.getOrderId()).isEqualTo(orderId);
        assertThat(capturedCommand.getQuantity()).isEqualTo(quantity);
        
        // Then - Verify response reflects reservation
        assertThat(response.getReservedStock()).isEqualTo(10);
        assertThat(response.getAvailableStock()).isEqualTo(90);
    }

    // ========== RELEASE RESERVATION TESTS ==========

    @Test
    void releaseReservation_shouldConvertParametersToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        String orderId = "ORDER-001";
        
        testProduct.reserveStockForOrder(orderId, 10);
        testProduct.clearDomainEvents();
        testProduct.releaseReservationForOrder(orderId);
        testProduct.clearDomainEvents();
        
        when(releaseReservationUseCase.execute(any(ReleaseReservationCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.releaseReservation(productIdString, orderId);
        
        // Then - Verify command creation
        ArgumentCaptor<ReleaseReservationCommand> commandCaptor = ArgumentCaptor.forClass(ReleaseReservationCommand.class);
        verify(releaseReservationUseCase).execute(commandCaptor.capture());
        
        ReleaseReservationCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        assertThat(capturedCommand.getOrderId()).isEqualTo(orderId);
        
        // Then - Verify response reflects release
        assertThat(response.getReservedStock()).isEqualTo(0);
        assertThat(response.getAvailableStock()).isEqualTo(100);
    }

    // ========== FULFILL RESERVATION TESTS ==========

    @Test
    void fulfillReservation_shouldConvertParametersToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        String orderId = "ORDER-001";
        
        testProduct.reserveStockForOrder(orderId, 10);
        testProduct.clearDomainEvents();
        testProduct.fulfillReservationForOrder(orderId);
        testProduct.clearDomainEvents();
        
        when(fulfillReservationUseCase.execute(any(FulfillReservationCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.fulfillReservation(productIdString, orderId);
        
        // Then - Verify command creation
        ArgumentCaptor<FulfillReservationCommand> commandCaptor = ArgumentCaptor.forClass(FulfillReservationCommand.class);
        verify(fulfillReservationUseCase).execute(commandCaptor.capture());
        
        FulfillReservationCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        assertThat(capturedCommand.getOrderId()).isEqualTo(orderId);
        
        // Then - Verify response reflects fulfillment
        assertThat(response.getTotalStock()).isEqualTo(90);
        assertThat(response.getReservedStock()).isEqualTo(0);
        assertThat(response.getAvailableStock()).isEqualTo(90);
    }

    // ========== UPDATE PRICE TESTS ==========

    @Test
    void updatePrice_shouldConvertParametersToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        BigDecimal newPrice = new BigDecimal("149.99");
        String currencyCode = "USD";  // Same currency as testProduct to avoid mismatch
        
        // Product from Product.create() is already active
        testProduct.updatePrice(Price.of(newPrice, Currency.getInstance(currencyCode)));
        testProduct.clearDomainEvents();
        
        when(updatePriceUseCase.execute(any(UpdatePriceCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.updatePrice(productIdString, newPrice, currencyCode);
        
        // Then - Verify command creation
        ArgumentCaptor<UpdatePriceCommand> commandCaptor = ArgumentCaptor.forClass(UpdatePriceCommand.class);
        verify(updatePriceUseCase).execute(commandCaptor.capture());
        
        UpdatePriceCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        assertThat(capturedCommand.getNewPrice().getAmount()).isEqualByComparingTo(newPrice);
        assertThat(capturedCommand.getNewPrice().getCurrency()).isEqualTo(Currency.getInstance(currencyCode));
        
        // Then - Verify response reflects new price
        assertThat(response.getPrice()).isEqualByComparingTo(newPrice);
        assertThat(response.getCurrency()).isEqualTo(currencyCode);
    }

    // ========== UPDATE PRODUCT DETAILS TESTS ==========

    @Test
    void updateProductDetails_shouldConvertParametersToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        String newName = "Updated Product Name";
        String newDescription = "Updated Description";
        
        // Product from Product.create() is already active
        testProduct.updateDetails(newName, newDescription);
        testProduct.clearDomainEvents();
        
        when(updateProductDetailsUseCase.execute(any(UpdateProductDetailsCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.updateProductDetails(productIdString, newName, newDescription);
        
        // Then - Verify command creation
        ArgumentCaptor<UpdateProductDetailsCommand> commandCaptor = ArgumentCaptor.forClass(UpdateProductDetailsCommand.class);
        verify(updateProductDetailsUseCase).execute(commandCaptor.capture());
        
        UpdateProductDetailsCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        assertThat(capturedCommand.getName()).isEqualTo(newName);
        assertThat(capturedCommand.getDescription()).isEqualTo(newDescription);
        
        // Then - Verify response reflects new details
        assertThat(response.getName()).isEqualTo(newName);
        assertThat(response.getDescription()).isEqualTo(newDescription);
    }

    // ========== ACTIVATE PRODUCT TESTS ==========

    @Test
    void activateProduct_shouldConvertIdToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        
        // Product from Product.create() is already active
        
        when(activateProductUseCase.execute(any(ActivateProductCommand.class))).thenReturn(testProduct);
        
        // When
        ProductResponse response = productFacade.activateProduct(productIdString);
        
        // Then - Verify command creation
        ArgumentCaptor<ActivateProductCommand> commandCaptor = ArgumentCaptor.forClass(ActivateProductCommand.class);
        verify(activateProductUseCase).execute(commandCaptor.capture());
        
        ActivateProductCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        
        // Then - Verify response reflects activation
        assertThat(response.isActive()).isTrue();
        assertThat(response.isVisible()).isTrue();
        assertThat(response.isAvailableForPurchase()).isTrue();
    }

    // ========== DEACTIVATE PRODUCT TESTS ==========

    @Test
    void deactivateProduct_shouldConvertIdToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        
        // Create a deactivated product for the use case to return
        Product deactivatedProduct = Product.create(testSku, "Test Product", "Test Description", testPrice, testStock);
        deactivatedProduct.clearDomainEvents();
        deactivatedProduct.deactivate();
        deactivatedProduct.clearDomainEvents();
        
        when(deactivateProductUseCase.execute(any(DeactivateProductCommand.class))).thenReturn(deactivatedProduct);
        
        // When
        ProductResponse response = productFacade.deactivateProduct(productIdString);
        
        // Then - Verify command creation
        ArgumentCaptor<DeactivateProductCommand> commandCaptor = ArgumentCaptor.forClass(DeactivateProductCommand.class);
        verify(deactivateProductUseCase).execute(commandCaptor.capture());
        
        DeactivateProductCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        
        // Then - Verify response reflects deactivation
        assertThat(response.isActive()).isFalse();
        assertThat(response.isVisible()).isTrue();  // visible is NOT changed by deactivate()
        assertThat(response.isAvailableForPurchase()).isFalse();
    }

    // ========== DISCONTINUE PRODUCT TESTS ==========

    @Test
    void discontinueProduct_shouldConvertIdToCommandAndCallUseCase() {
        // Given
        String productIdString = testProductId.getValue();
        
        // Create a discontinued product for the use case to return
        Product discontinuedProduct = Product.create(testSku, "Test Product", "Test Description", testPrice, testStock);
        discontinuedProduct.clearDomainEvents();
        discontinuedProduct.discontinue();
        discontinuedProduct.clearDomainEvents();
        
        when(discontinueProductUseCase.execute(any(DiscontinueProductCommand.class))).thenReturn(discontinuedProduct);
        
        // When
        ProductResponse response = productFacade.discontinueProduct(productIdString);
        
        // Then - Verify command creation
        ArgumentCaptor<DiscontinueProductCommand> commandCaptor = ArgumentCaptor.forClass(DiscontinueProductCommand.class);
        verify(discontinueProductUseCase).execute(commandCaptor.capture());
        
        DiscontinueProductCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.getProductId().getValue()).isEqualTo(productIdString);
        
        // Then - Verify response reflects discontinuation
        assertThat(response.isDiscontinued()).isTrue();
        assertThat(response.isActive()).isFalse();
        assertThat(response.isVisible()).isTrue();  // Check actual domain behavior
        assertThat(response.isAvailableForPurchase()).isFalse();
    }

    // ========== DTO CONVERSION TESTS ==========

    @Test
    void productResponse_fromDomain_shouldMapAllProductFields() {
        // Given - Product with reservations (already active from Product.create())
        testProduct.reserveStockForOrder("ORDER-001", 25);
        testProduct.clearDomainEvents();
        
        // When
        ProductResponse response = ProductResponse.fromDomain(testProduct);
        
        // Then - All fields mapped correctly
        assertThat(response.getId()).isEqualTo(testProduct.getId().getValue());
        assertThat(response.getSku()).isEqualTo(testProduct.getSku().getValue());
        assertThat(response.getName()).isEqualTo(testProduct.getName());
        assertThat(response.getDescription()).isEqualTo(testProduct.getDescription());
        assertThat(response.getPrice()).isEqualByComparingTo(testProduct.getPrice().getAmount());
        assertThat(response.getCurrency()).isEqualTo(testProduct.getPrice().getCurrency().getCurrencyCode());
        assertThat(response.getTotalStock()).isEqualTo(testProduct.getStock().getQuantity());
        assertThat(response.getAvailableStock()).isEqualTo(testProduct.getStock().getAvailableQuantity());
        assertThat(response.getReservedStock()).isEqualTo(testProduct.getStock().getReservedQuantity());
        assertThat(response.isActive()).isEqualTo(testProduct.isActive());
        assertThat(response.isVisible()).isEqualTo(testProduct.isVisible());
        assertThat(response.isAvailableForPurchase()).isEqualTo(testProduct.isAvailableForPurchase());
        assertThat(response.isDiscontinued()).isEqualTo(testProduct.isDiscontinued());
    }

    @Test
    void productResponse_fromDomain_shouldHandleInactiveProduct() {
        // Given - Create an inactive product
        Product inactiveProduct = Product.create(testSku, "Test Product", "Test Description", testPrice, testStock);
        inactiveProduct.clearDomainEvents();
        inactiveProduct.deactivate();
        inactiveProduct.clearDomainEvents();
        
        // When
        ProductResponse response = ProductResponse.fromDomain(inactiveProduct);
        
        // Then - Inactive state mapped
        assertThat(response.isActive()).isFalse();
        assertThat(response.isVisible()).isTrue();  // deactivate() doesn't change visible
        assertThat(response.isAvailableForPurchase()).isFalse();
        assertThat(response.isDiscontinued()).isFalse();
    }

    @Test
    void productResponse_fromDomain_shouldHandleDiscontinuedProduct() {
        // Given - Create a discontinued product
        Product discontinuedProduct = Product.create(testSku, "Test Product", "Test Description", testPrice, testStock);
        discontinuedProduct.clearDomainEvents();
        discontinuedProduct.discontinue();
        discontinuedProduct.clearDomainEvents();
        
        // When
        ProductResponse response = ProductResponse.fromDomain(discontinuedProduct);
        
        // Then - Discontinued state mapped
        assertThat(response.isDiscontinued()).isTrue();
        assertThat(response.isActive()).isFalse();
        assertThat(response.isVisible()).isTrue();  // Check domain logic
        assertThat(response.isAvailableForPurchase()).isFalse();
    }

    @Test
    void productResponse_fromDomain_shouldHandleMultipleCurrencies() {
        // Given - Product with EUR currency
        Price eurPrice = Price.of(new BigDecimal("49.99"), Currency.getInstance("EUR"));
        Product eurProduct = Product.create(
            SKU.of("EUR-SKU"),
            "EUR Product",
            "EUR Description",
            eurPrice,
            Stock.of(50)
        );
        eurProduct.clearDomainEvents();
        
        // When
        ProductResponse response = ProductResponse.fromDomain(eurProduct);
        
        // Then - EUR currency mapped
        assertThat(response.getCurrency()).isEqualTo("EUR");
        assertThat(response.getPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
    }
}
