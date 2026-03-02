package com.mustapha.ecommerce.order.application.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * Port for tax calculation operations
 * Supports multiple tax jurisdictions (Egypt, UAE, Saudi Arabia, etc.)
 */
public interface TaxCalculationPort {

    /**
     * Calculate tax for an order
     * @return Tax calculation result
     */
    TaxCalculation calculateTax(TaxCalculationRequest request);

    /**
     * Get tax rate for a country
     * @param countryCode ISO 2-letter country code (EG, AE, SA, etc.)
     * @return Tax rate (0.0 - 1.0)
     */
    BigDecimal getTaxRateForCountry(String countryCode);

    /**
     * Check if customer is tax exempt
     */
    boolean isTaxExempt(Long customerId, String countryCode);

    record TaxCalculationRequest(
        Long orderId,
        Long customerId,
        BigDecimal subtotal,
        String shippingCountryCode,
        String billingCountryCode,
        String customerType,  // INDIVIDUAL, BUSINESS
        String taxId,         // VAT/Tax registration number (for B2B)
        List<OrderLineItem> items
    ) {}

    record OrderLineItem(
        Long productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        String taxCategory  // STANDARD, REDUCED, ZERO, EXEMPT
    ) {}

    record TaxCalculation(
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal total,
        String taxRate,
        String jurisdiction,
        String taxType,  // VAT, GST, SALES_TAX
        boolean isTaxExempt,
        List<TaxBreakdown> breakdown // Per-item tax breakdown
    ) {}

    record TaxBreakdown(
        Long productId,
        String productName,
        BigDecimal amount,
        BigDecimal tax,
        String taxRate,
        String taxCategory
    ) {}
}
