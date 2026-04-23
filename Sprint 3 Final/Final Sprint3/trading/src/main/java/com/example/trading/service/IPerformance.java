package com.example.trading.service;

import com.example.trading.model.PerformanceIndicator;
import java.time.LocalDate;
import java.util.List;

/**
 * Performance analysis contract — implemented by PerformanceService.
 */
public interface IPerformance {
    List<PerformanceIndicator> calculateIndicators(String symbol,
                                                   LocalDate startDate,
                                                   LocalDate endDate);

    /**
     * Returns a summary keyed by metric name (e.g. "percentReturn", "high", "low").
     * Using a loose return type keeps this interface decoupled from
     * PerformanceService's inner PerformanceSummary class.
     */
    java.util.Map<String, Object> getSummary(String symbol,
                                             LocalDate startDate,
                                             LocalDate endDate);
}
