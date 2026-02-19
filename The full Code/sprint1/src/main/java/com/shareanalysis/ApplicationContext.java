package com.shareanalysis;

import com.shareanalysis.api.ShareDataProvider;
import com.shareanalysis.api.YahooFinanceProvider;
import com.shareanalysis.repository.ShareRepository;
import com.shareanalysis.repository.JsonShareRepository;
import com.shareanalysis.service.SharePriceService;
import com.shareanalysis.service.SharePriceServiceImpl;
import com.shareanalysis.ui.ConsoleView;

/**
 * ApplicationContext wires together all architectural components.
 *
 * In Sprint 1 this demonstrates Simple Architecture:
 *  - UI Layer        → ConsoleView
 *  - Service Layer   → SharePriceService
 *  - Data Layer      → ShareRepository (local persistence)
 *  - External API    → ShareDataProvider (remote data)
 *
 * This class acts as a lightweight dependency-injection container.
 */
public class ApplicationContext {

    private ShareDataProvider dataProvider;
    private ShareRepository   repository;
    private SharePriceService service;
    private ConsoleView       view;

    /**
     * Instantiate and wire all components.
     */
    public void initialise() {
        System.out.println("[Context] Initialising components...");

        dataProvider = new YahooFinanceProvider();
        repository   = new JsonShareRepository();
        service      = new SharePriceServiceImpl(dataProvider, repository);
        view         = new ConsoleView(service);

        System.out.println("[Context] All components wired successfully.");
    }

    /**
     * Start the application (abstract demo run for Sprint 1).
     */
    public void run() {
        System.out.println("[Context] Starting application...");
        view.start();
    }
}
