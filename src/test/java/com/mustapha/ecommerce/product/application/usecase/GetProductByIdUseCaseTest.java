package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.GetProductByIdQuery;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
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
import static org.mockito.Mockito.*;

/**
 * GetProductByIdUseCase Tests
 * Tests: Product retrieval by ID, not found handling
 */
@ExtendWith(MockitoExtension.class)
class GetProductByIdUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    private GetProductByIdUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetProductByIdUseCase(productRepository);
    }

    @Test
    void shouldGetProductById() {
        ProductId productId = ProductId.generate();
        Product expectedProduct = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("99.99"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        GetProductByIdQuery query = new GetProductByIdQuery(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(expectedProduct));

        Product result = useCase.execute(query);

        assertNotNull(result);
        assertEquals(expectedProduct, result);
        verify(productRepository).findById(productId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        ProductId productId = ProductId.generate();
        GetProductByIdQuery query = new GetProductByIdQuery(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            useCase.execute(query);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository).findById(productId);
    }
}
