package com.example.demo1.service;

import com.example.demo1.model.SharePrice;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of ShareDataService.
 * Will connect to Yahoo Finance API in Sprint 2.
 * Currently returns synthetic/mock data for testing purposes.
 */
public class YahooFinanceService implements ShareDataService {

    private static final int MAX_RANGE_YEARS = 2;

    @Override
    public List<SharePrice> fetchPrices(String symbol, LocalDate startDate, LocalDate endDate) {
        // Validate date range does not exceed 2 years
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_RANGE_YEARS * 365L) {
            throw new IllegalArgumentException(
                "Date range must not exceed " + MAX_RANGE_YEARS + " years.");
        }

        // TODO Sprint 2: Replace with real Yahoo Finance HTTP request
        return generateMockData(symbol, startDate, endDate);
    }

    /**
     * Generates mock share price data for development/testing.
     */
    private List<SharePrice> generateMockData(String symbol, LocalDate start, LocalDate end) {
        List<SharePrice> prices = new ArrayList<>();
        double price = 150.0; // Starting mock price
        LocalDate current = start;

        while (!current.isAfter(end)) {
            // Skip weekends (markets are closed)
            if (current.getDayOfWeek().getValue() < 6) {
                // Simulate small random price movement
                price += (Math.random() - 0.5) * 5;
                prices.add(new SharePrice(
                        symbol, current,
                        price - 1, price, price + 2, price - 2, 1_000_000L
                ));
            }
            current = current.plusDays(1);
        }
        return prices;
    }
}
