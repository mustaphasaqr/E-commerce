package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.GetProductBySkuQuery;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * GetProductBySkuUseCase Tests
 * Tests: Product retrieval by SKU, SKU validation, not found handling
 */
@ExtendWith(MockitoExtension.class)
class GetProductBySkuUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductBySkuUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductBySkuUseCase(productRepository);
    }

    @Test
    void shouldGetProductBySku() {
        GetProductBySkuQuery query = new GetProductBySkuQuery("PROD-123");
        Product expectedProduct = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        when(productRepository.findBySku(any(SKU.class))).thenReturn(Optional.of(expectedProduct));

        Product result = useCase.execute(query);

        assertNotNull(result);
        assertEquals(expectedProduct, result);
        verify(productRepository).findBySku(any(SKU.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        GetProductBySkuQuery query = new GetProductBySkuQuery("PROD-999");

        when(productRepository.findBySku(any(SKU.class))).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            useCase.execute(query);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository).findBySku(any(SKU.class));
    }

    @Test
    void shouldNormalizeSkuBeforeQuerying() {
        GetProductBySkuQuery query = new GetProductBySkuQuery("prod-123");
        Product expectedProduct = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        when(productRepository.findBySku(SKU.of("PROD-123"))).thenReturn(Optional.of(expectedProduct));

        Product result = useCase.execute(query);

        assertNotNull(result);
        verify(productRepository).findBySku(SKU.of("PROD-123"));
    }
}
