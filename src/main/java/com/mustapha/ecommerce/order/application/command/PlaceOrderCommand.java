package com.mustapha.ecommerce.order.application.command;

import java.util.List;

import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Place Order Command (Input DTO)
 * Responsibility: Transfer data from API layer to Application layer
 * Pattern: Command (CQS - Command Query Separation)
 * 
 * Note: Uses value objects (CustomerId, ProductId, Money) for type safety
 * The Facade converts primitives from API → value objects for this command
 */
public class PlaceOrderCommand {
    
    private final CustomerId customerId;
    private final List<OrderItemData> items;
    private final String shippingCity;
    private final String shippingState;
    private final String shippingCountry;
    private final String shippingZipCode;
    private final String utmSource;
    private final String utmCampaign;
    private final String referrer;
    private final Long cartId;
    
    public PlaceOrderCommand(CustomerId customerId, List<OrderItemData> items,
                            String shippingCity, String shippingState, String shippingCountry, String shippingZipCode,
                            String utmSource, String utmCampaign, String referrer, Long cartId) {
        this.customerId = customerId;
        this.items = items;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingCountry = shippingCountry;
        this.shippingZipCode = shippingZipCode;
        this.utmSource = utmSource;
        this.utmCampaign = utmCampaign;
        this.referrer = referrer;
        this.cartId = cartId;
    }
    
    public CustomerId getCustomerId() {
        return customerId;
    }
    
    public List<OrderItemData> getItems() {
        return items;
    }
    
    public String getShippingCity() {
        return shippingCity;
    }
    
    public String getShippingState() {
        return shippingState;
    }
    
    public String getShippingCountry() {
        return shippingCountry;
    }
    
    public String getShippingZipCode() {
        return shippingZipCode;
    }
    
    public String getUtmSource() {
        return utmSource;
    }
    
    public String getUtmCampaign() {
        return utmCampaign;
    }
    
    public String getReferrer() {
        return referrer;
    }
    
    public Long getCartId() {
        return cartId;
    }
    
    /**
     * Order Item Data (nested DTO)
     * Uses value objects for type safety at application layer boundary
     */
    public static class OrderItemData {
        private final ProductId productId;
        private final String productName;
        private final int quantity;
        private final Money unitPrice;
        
        public OrderItemData(ProductId productId, String productName, int quantity, Money unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public ProductId getProductId() {
            return productId;
        }
        
        public String getProductName() {
            return productName;
        }
        
        public int getQuantity() {
            return quantity;
        }
        
        public Money getUnitPrice() {
            return unitPrice;
        }
    }
}
