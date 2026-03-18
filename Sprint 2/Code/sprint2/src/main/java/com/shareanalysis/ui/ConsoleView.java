package com.shareanalysis.ui;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.Alert;
import com.shareanalysis.model.Annotation;
import com.shareanalysis.model.ShareQuery;
import com.shareanalysis.service.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Console UI — drives all components as a Sprint 2 demonstration.
 *
 * Exercises every component from the diagram:
 *   SharePriceService, PriceComparisonService, AlertService,
 *   PerformanceService, ChartingService, AccountWalletService,
 *   AnnotationService, and the API layer.
 */
public class ConsoleView {

    private final SharePriceService      sharePriceService;
    private final PriceComparisonService comparisonService;
    private final AlertService           alertService;
    private final ChartingService        chartingService;
    private final AccountWalletService   accountService;
    private final AnnotationService      annotationService;

    public ConsoleView(SharePriceService sharePriceService,
                       PriceComparisonService comparisonService,
                       AlertService alertService,
                       ChartingService chartingService,
                       AccountWalletService accountService,
                       AnnotationService annotationService) {
        this.sharePriceService = sharePriceService;
        this.comparisonService = comparisonService;
        this.alertService      = alertService;
        this.chartingService   = chartingService;
        this.accountService    = accountService;
        this.annotationService = annotationService;
    }

    public void start() {
        System.out.println("=== Share Price Technical Analysis App — Sprint 2 ===");
        System.out.println("Architecture: Component-based with SharePriceService as hub");
        System.out.println();

        demoChart();
        demoComparison();
        demoAlerts();
        demoAnnotations();
        demoAccount();
    }

    // Chart + Performance
    private void demoChart() {
        System.out.println("--- ChartingService + PerformanceService ---");
        try {
            ShareQuery query = new ShareQuery("AAPL",
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31));

            // Add an annotation before rendering
            annotationService.addAnnotation("AAPL", LocalDate.of(2024, 2, 1),
                    "Q1 earnings report — strong results");

            List<Annotation> annotations = annotationService.getAnnotations("AAPL");
            chartingService.renderChart(query, annotations);

        } catch (ServiceException e) {
            System.out.println("[Error] " + e.getMessage());
        }
        System.out.println();
    }

    // Price Comparison
    private void demoComparison() {
        System.out.println("--- PriceComparisonService ---");
        try {
            ShareQuery q1 = new ShareQuery("AAPL",
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
            ShareQuery q2 = new ShareQuery("MSFT",
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

            String result = comparisonService.compare(q1, q2);
            System.out.println("  " + result);

        } catch (ServiceException e) {
            System.out.println("[Error] " + e.getMessage());
        }
        System.out.println();
    }

    // Alerts
    private void demoAlerts() {
        System.out.println("--- AlertService ---");
        try {
            // Create an alert
            Alert alert = alertService.createAlert("AAPL", 50.0, Alert.Condition.ABOVE);

            // Evaluate against live data
            ShareQuery query = new ShareQuery("AAPL",
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
            List<Alert> triggered = alertService.evaluateAlerts(query);

            if (triggered.isEmpty()) {
                System.out.println("  No alerts triggered.");
            } else {
                triggered.forEach(a -> System.out.println("  ALERT FIRED: " + a));
            }

        } catch (ServiceException e) {
            System.out.println("[Error] " + e.getMessage());
        }
        System.out.println();
    }

    // Annotations
    private void demoAnnotations() {
        System.out.println("--- AnnotationService ---");
        annotationService.getAllAnnotations()
                .forEach(a -> System.out.println("  " + a));
        System.out.println();
    }

    // Account + Wallet
    private void demoAccount() {
        System.out.println("--- AccountWalletService ---");
        accountService.createAccount("ACC-001", "jonathan", 1000.00);
        accountService.deposit("ACC-001", 500.00);
        accountService.withdraw("ACC-001", 200.00);
        accountService.getAccount("ACC-001")
                .ifPresent(a -> System.out.println("  Final: " + a));
        System.out.println();
    }
}
