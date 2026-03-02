package com.mustapha.ecommerce.order.infrastructure.adapter.tax;

import com.mustapha.ecommerce.order.application.port.TaxCalculationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rule-based tax calculation adapter for Middle East markets
 * Supports Egypt, UAE, Saudi Arabia, and other GCC countries
 */
@Slf4j
@Component
public class TaxCalculationAdapter implements TaxCalculationPort {

    // Tax rates by country (can be configured via application.properties)
    private final Map<String, TaxConfig> taxRates;

    public TaxCalculationAdapter(
        @Value("${tax.egypt.vat-rate:0.14}") BigDecimal egyptVatRate,
        @Value("${tax.uae.vat-rate:0.05}") BigDecimal uaeVatRate,
        @Value("${tax.saudi.vat-rate:0.15}") BigDecimal saudiVatRate,
        @Value("${tax.bahrain.vat-rate:0.10}") BigDecimal bahrainVatRate,
        @Value("${tax.kuwait.vat-rate:0.00}") BigDecimal kuwaitVatRate,
        @Value("${tax.oman.vat-rate:0.05}") BigDecimal omanVatRate,
        @Value("${tax.qatar.vat-rate:0.00}") BigDecimal qatarVatRate
    ) {
        this.taxRates = Map.of(
            "EG", new TaxConfig("Egypt", egyptVatRate, "VAT"),
            "AE", new TaxConfig("United Arab Emirates", uaeVatRate, "VAT"),
            "SA", new TaxConfig("Saudi Arabia", saudiVatRate, "VAT"),
            "BH", new TaxConfig("Bahrain", bahrainVatRate, "VAT"),
            "KW", new TaxConfig("Kuwait", kuwaitVatRate, "No VAT"),
            "OM", new TaxConfig("Oman", omanVatRate, "VAT"),
            "QA", new TaxConfig("Qatar", qatarVatRate, "No VAT")
        );
        
        log.info("💰 Tax calculation service initialized with rates:");
        taxRates.forEach((code, config) -> 
            log.info("   {} ({}): {}% {}", config.countryName, code, 
                config.rate.multiply(BigDecimal.valueOf(100)), config.taxType)
        );
    }

    @Override
    public TaxCalculation calculateTax(TaxCalculationRequest request) {
        log.info("💰 Calculating tax for order {} (country: {}, customer type: {})", 
            request.orderId(), request.shippingCountryCode(), request.customerType());

        // Check if tax exempt
        boolean isTaxExempt = isTaxExempt(request.customerId(), request.shippingCountryCode());
        if (isTaxExempt) {
            log.info("✅ Customer {} is tax exempt", request.customerId());
            return createExemptCalculation(request);
        }

        // Get tax config for country
        TaxConfig taxConfig = taxRates.getOrDefault(
            request.shippingCountryCode().toUpperCase(), 
            new TaxConfig("Unknown", BigDecimal.ZERO, "No Tax")
        );

        // Calculate per-item tax breakdown
        List<TaxBreakdown> breakdown = new ArrayList<>();
        BigDecimal totalTax = BigDecimal.ZERO;

        for (OrderLineItem item : request.items()) {
            BigDecimal itemAmount = item.unitPrice().multiply(BigDecimal.valueOf(item.quantity()));
            BigDecimal itemTaxRate = getTaxRateForCategory(item.taxCategory(), taxConfig.rate);
            BigDecimal itemTax = itemAmount.multiply(itemTaxRate)
                .setScale(2, RoundingMode.HALF_UP);

            breakdown.add(new TaxBreakdown(
                item.productId(),
                item.productName(),
                itemAmount,
                itemTax,
                formatRate(itemTaxRate),
                item.taxCategory()
            ));

            totalTax = totalTax.add(itemTax);
        }

        BigDecimal total = request.subtotal().add(totalTax);

        log.info("✅ Tax calculated: subtotal={}, tax={}, total={} (rate: {})", 
            request.subtotal(), totalTax, total, formatRate(taxConfig.rate));

        return new TaxCalculation(
            request.subtotal(),
            totalTax,
            total,
            formatRate(taxConfig.rate),
            taxConfig.countryName,
            taxConfig.taxType,
            false,
            breakdown
        );
    }

    @Override
    public BigDecimal getTaxRateForCountry(String countryCode) {
        TaxConfig config = taxRates.getOrDefault(
            countryCode.toUpperCase(), 
            new TaxConfig("Unknown", BigDecimal.ZERO, "No Tax")
        );
        return config.rate;
    }

    @Override
    public boolean isTaxExempt(Long customerId, String countryCode) {
        // TODO: Check customer tax exemption status from database
        // For now, return false (no exemptions)
        // In production:
        // - Check if customer has valid tax exemption certificate
        // - Check if customer is a registered business with valid VAT number
        // - Check if customer is a government entity
        // - Check if customer is a diplomatic mission
        return false;
    }

    private TaxCalculation createExemptCalculation(TaxCalculationRequest request) {
        List<TaxBreakdown> breakdown = request.items().stream()
            .map(item -> new TaxBreakdown(
                item.productId(),
                item.productName(),
                item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())),
                BigDecimal.ZERO,
                "0%",
                "EXEMPT"
            ))
            .toList();

        return new TaxCalculation(
            request.subtotal(),
            BigDecimal.ZERO,
            request.subtotal(),
            "0%",
            "Tax Exempt",
            "EXEMPT",
            true,
            breakdown
        );
    }

    private BigDecimal getTaxRateForCategory(String taxCategory, BigDecimal standardRate) {
        if (taxCategory == null) {
            return standardRate;
        }

        return switch (taxCategory.toUpperCase()) {
            case "STANDARD" -> standardRate;
            case "REDUCED" -> standardRate.multiply(BigDecimal.valueOf(0.5)); // Half rate
            case "ZERO", "EXEMPT" -> BigDecimal.ZERO;
            default -> standardRate;
        };
    }

    private String formatRate(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP) + "%";
    }

    private record TaxConfig(
        String countryName,
        BigDecimal rate,
        String taxType
    ) {}
}
