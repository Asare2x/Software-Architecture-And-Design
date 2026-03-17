package com.shareanalysis;

import com.shareanalysis.api.YahooFinanceProvider;
import com.shareanalysis.repository.InMemoryAccountRepository;
import com.shareanalysis.repository.JsonSharePriceRepository;
import com.shareanalysis.service.*;
import com.shareanalysis.ui.ConsoleView;

/**
 * ApplicationContext — wires all components together.
 *
 * Sprint 2: Component diagram realised in code.
 *
 * Component connections wired here:
 *   Data Market (YahooFinanceProvider) → SharePriceService
 *   SharePriceRepository               → SharePriceService
 *   SharePriceService                  → PriceComparisonService
 *   SharePriceService                  → AlertService
 *   SharePriceService                  → PerformanceService
 *   SharePriceService + PerformanceService → ChartingService
 *   AccountRepository                  → AccountWalletService
 *   All services                       → ConsoleView (UI)
 */
public class ApplicationContext {

    private ConsoleView view;

    public void initialise() {
        System.out.println("[ApplicationContext] Wiring components...");

        // Infrastructure
        YahooFinanceProvider     dataProvider    = new YahooFinanceProvider();
        JsonSharePriceRepository shareRepository = new JsonSharePriceRepository();
        InMemoryAccountRepository accountRepo    = new InMemoryAccountRepository();

        // Central hub
        SharePriceService sharePriceService = new SharePriceService(dataProvider, shareRepository);

        // Services connected to SharePriceService
        PriceComparisonService comparisonService  = new PriceComparisonService(sharePriceService);
        AlertService           alertService       = new AlertService(sharePriceService);
        PerformanceService     performanceService = new PerformanceService(sharePriceService);
        ChartingService        chartingService    = new ChartingService(sharePriceService, performanceService);
        AccountWalletService   accountService     = new AccountWalletService(accountRepo);
        AnnotationService      annotationService  = new AnnotationService();

        // UI
        view = new ConsoleView(
            sharePriceService,
            comparisonService,
            alertService,
            chartingService,
            accountService,
            annotationService
        );

        System.out.println("[ApplicationContext] All components wired.");
    }

    public void run() {
        view.start();
    }
}
