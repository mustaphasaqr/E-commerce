package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.CreateProductCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.ProductCreatedEvent;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateProductUseCase Tests
 * Tests: Product creation orchestration, SKU uniqueness, event publishing
 */
@ExtendWith(MockitoExtension.class)
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private CreateProductUseCase useCase;

    private SKU validSku;
    private Price validPrice;
    private Stock validStock;

    @BeforeEach
    void setUp() {
        useCase = new CreateProductUseCase(productRepository, eventPublisher);
        validSku = SKU.of("PROD-123");
        validPrice = Price.of(new BigDecimal("99.99"), Currency.getInstance("USD"));
        validStock = Stock.of(100);
    }

    @Test
    void shouldCreateProduct() {
        CreateProductCommand command = new CreateProductCommand(
            validSku, "Test Product", "Description", validPrice, validStock
        );

        when(productRepository.existsBySku(validSku)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Description", result.getDescription());
        verify(productRepository).existsBySku(validSku);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldPublishProductCreatedEvent() {
        CreateProductCommand command = new CreateProductCommand(
            validSku, "Test Product", "Description", validPrice, validStock
        );

        when(productRepository.existsBySku(validSku)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        ArgumentCaptor<ProductDomainEvent> eventCaptor = ArgumentCaptor.forClass(ProductDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        ProductDomainEvent event = eventCaptor.getValue();
        assertTrue(event instanceof ProductCreatedEvent);
    }

    @Test
    void shouldRejectDuplicateSku() {
        CreateProductCommand command = new CreateProductCommand(
            validSku, "Test Product", "Description", validPrice, validStock
        );

        when(productRepository.existsBySku(validSku)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        verify(productRepository).existsBySku(validSku);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldClearDomainEventsAfterPublishing() {
        CreateProductCommand command = new CreateProductCommand(
            validSku, "Test Product", "Description", validPrice, validStock
        );

        when(productRepository.existsBySku(validSku)).thenReturn(false);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        when(productRepository.save(productCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        Product savedProduct = productCaptor.getValue();
        assertEquals(0, savedProduct.getDomainEvents().size());
    }
}
