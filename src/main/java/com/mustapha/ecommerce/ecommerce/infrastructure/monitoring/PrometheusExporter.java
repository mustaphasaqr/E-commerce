package com.mustapha.ecommerce.ecommerce.infrastructure.monitoring;

import org.springframework.stereotype.Component;

/**
 * Prometheus Exporter
 * Responsibility: Export metrics to Prometheus
 */
@Component
public class PrometheusExporter {

    public void recordMetric(String name, double value) {
        // Export metrics to Prometheus
        System.out.println("Recording metric: " + name + " = " + value);
    }
}
