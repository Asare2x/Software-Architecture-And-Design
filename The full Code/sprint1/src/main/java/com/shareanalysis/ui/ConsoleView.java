package com.shareanalysis.ui;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;
import com.shareanalysis.service.ServiceException;
import com.shareanalysis.service.SharePriceService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Console-based UI component for Sprint 1.
 *
 * Architecture role: Presentation / UI Layer
 *
 * This layer is deliberately thin: it translates user input into
 * {@link ShareQuery} objects, delegates all business logic to the
 * {@link SharePriceService}, and renders results to stdout.
 *
 * Sprint 3 will replace/supplement this with a proper web front-end.
 * The UI layer is decoupled from data concerns via the service interface,
 * meaning this swap requires no changes to lower layers.
 */
public class ConsoleView {

    private final SharePriceService service;

    public ConsoleView(SharePriceService service) {
        this.service = service;
    }

    /**
     * Entry point for the Sprint 1 demonstration run.
     *
     * Simulates a user querying two companies and comparing them.
     */
    public void start() {
        System.out.println("\n--- Sprint 1 Architectural Demonstration ---\n");

        // Demo query 1: single company
        demonstrateSingleQuery("AAPL",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 31));

        System.out.println();

        // Demo query 2: comparison between two companies
        demonstrateComparison(
                "AAPL", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31),
                "MSFT", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void demonstrateSingleQuery(String symbol, LocalDate from, LocalDate to) {
        System.out.printf("=== Single Query: %s  (%s → %s) ===%n", symbol, from, to);
        try {
            ShareQuery query = new ShareQuery(symbol, from, to);
            List<SharePrice> prices = service.getPrices(query);

            // Print a summary table (first & last 3 records)
            printPriceSummary(prices);

            // Demonstrate SMA calculation
            List<Double> sma20 = service.calculateSMA(prices, 20);
            System.out.printf("  20-day SMA on last record: %.2f%n",
                    sma20.isEmpty() ? 0 : sma20.get(sma20.size() - 1));

        } catch (IllegalArgumentException e) {
            System.out.println("  [Validation Error] " + e.getMessage());
        } catch (ServiceException e) {
            System.out.println("  [Service Error] " + e.getMessage());
        }
    }

    private void demonstrateComparison(String sym1, LocalDate from1, LocalDate to1,
                                       String sym2, LocalDate from2, LocalDate to2) {
        System.out.printf("=== Comparison: %s vs %s ===%n", sym1, sym2);
        try {
            ShareQuery q1 = new ShareQuery(sym1, from1, to1);
            ShareQuery q2 = new ShareQuery(sym2, from2, to2);
            Map<String, List<SharePrice>> results = service.comparePrices(q1, q2);

            results.forEach((symbol, prices) -> {
                double first = prices.isEmpty() ? 0 : prices.get(0).getClose();
                double last  = prices.isEmpty() ? 0 : prices.get(prices.size() - 1).getClose();
                double change = last - first;
                System.out.printf("  %-5s | Records: %3d | First close: %7.2f | Last close: %7.2f | Change: %+.2f%n",
                        symbol, prices.size(), first, last, change);
            });

        } catch (ServiceException e) {
            System.out.println("  [Service Error] " + e.getMessage());
        }
    }

    private void printPriceSummary(List<SharePrice> prices) {
        if (prices.isEmpty()) {
            System.out.println("  No data returned.");
            return;
        }
        System.out.printf("  Total records: %d%n", prices.size());
        System.out.println("  Sample records:");
        System.out.printf("  %-12s %-10s %-10s %-10s %-12s%n",
                "Date", "Open", "High", "Low", "Close");
        System.out.println("  " + "-".repeat(58));

        // Print first 3
        prices.stream().limit(3).forEach(this::printRow);
        if (prices.size() > 6) System.out.println("  ...");
        // Print last 3
        prices.stream().skip(Math.max(0, prices.size() - 3)).forEach(this::printRow);
    }

    private void printRow(SharePrice p) {
        System.out.printf("  %-12s %-10.2f %-10.2f %-10.2f %-12.2f%n",
                p.getDate(), p.getOpen(), p.getHigh(), p.getLow(), p.getClose());
    }
}
