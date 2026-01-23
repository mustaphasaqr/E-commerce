package com.mustapha.ecommerce.product.application.usecase;

import com.mustapha.ecommerce.product.application.command.UpdatePriceCommand;
import com.mustapha.ecommerce.product.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.event.PriceChangedEvent;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UpdatePriceUseCase Tests
 * Tests: Price update, currency validation, event publishing
 */
@ExtendWith(MockitoExtension.class)
class UpdatePriceUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private DomainEventPublisher eventPublisher;

    private UpdatePriceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePriceUseCase(productRepository, eventPublisher);
    }

    @Test
    void shouldUpdatePrice() {
        ProductId productId = ProductId.generate();
        Currency usd = Currency.getInstance("USD");
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("100.00"), usd),
            Stock.of(100)
        );

        Price newPrice = Price.of(new BigDecimal("150.00"), usd);
        UpdatePriceCommand command = new UpdatePriceCommand(productId, newPrice);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = useCase.execute(command);

        assertNotNull(result);
        assertEquals(newPrice, result.getPrice());
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
    }

    @Test
    void shouldPublishPriceChangedEvent() {
        ProductId productId = ProductId.generate();
        Currency usd = Currency.getInstance("USD");
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("100.00"), usd),
            Stock.of(100)
        );
        product.clearDomainEvents(); // Clear creation event

        Price newPrice = Price.of(new BigDecimal("150.00"), usd);
        UpdatePriceCommand command = new UpdatePriceCommand(productId, newPrice);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(command);

        ArgumentCaptor<ProductDomainEvent> eventCaptor = ArgumentCaptor.forClass(ProductDomainEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        ProductDomainEvent event = eventCaptor.getValue();
        assertTrue(event instanceof PriceChangedEvent);
    }

    @Test
    void shouldRejectCurrencyChange() {
        ProductId productId = ProductId.generate();
        Product product = Product.create(
            SKU.of("PROD-123"),
            "Test Product",
            "Description",
            Price.of(new BigDecimal("100.00"), Currency.getInstance("USD")),
            Stock.of(100)
        );

        Price newPrice = Price.of(new BigDecimal("150.00"), Currency.getInstance("EUR"));
        UpdatePriceCommand command = new UpdatePriceCommand(productId, newPrice);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("currency") || exception.getMessage().contains("different currencies"));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound() {
        ProductId productId = ProductId.generate();
        Price newPrice = Price.of(new BigDecimal("150.00"), Currency.getInstance("USD"));
        UpdatePriceCommand command = new UpdatePriceCommand(productId, newPrice);

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ProductNotFoundException.class, () -> {
            useCase.execute(command);
        });

        assertTrue(exception.getMessage().contains("not found"));
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(eventPublisher, never()).publish(any());
    }
}
