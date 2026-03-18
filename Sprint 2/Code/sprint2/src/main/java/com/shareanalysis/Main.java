package com.shareanalysis;

/**
 * Main entry point.
 *
 * Sprint 2 — Share Price Technical Analysis Application
 * Architecture: Component-based design matching the Sprint 2 component diagram.
 *
 * Components: SharePriceService (hub), PriceComparisonService, AlertService,
 *             PerformanceService, ChartingService, AccountWalletService,
 *             AnnotationService, SharePriceRepository, AccountRepository,
 *             DataMarket (YahooFinanceProvider), API layer.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Share Price Technical Analysis — Sprint 2 ===");
        System.out.println();

        ApplicationContext context = new ApplicationContext();
        context.initialise();
        System.out.println();
        context.run();
    }
}
