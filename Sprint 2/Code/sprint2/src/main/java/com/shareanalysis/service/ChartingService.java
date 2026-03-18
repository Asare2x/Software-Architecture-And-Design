package com.shareanalysis.service;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.Annotation;
import com.shareanalysis.model.PerformanceIndicator;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Component: Charting Service
 * Directions:
 *   Charting Service ← SharePriceService   (needs price data)
 *   Charting Service ← Performance Service  (needs indicators)
 *
 * Combines price data and performance indicators into a renderable chart.
 * In Sprint 3 this will produce actual chart output (HTML/SVG via a chart library).
 * For Sprint 2 it prints an ASCII summary to the console.
 */
public class ChartingService {

    private final SharePriceService  sharePriceService;
    private final PerformanceService performanceService;

    public ChartingService(SharePriceService sharePriceService,
                           PerformanceService performanceService) {
        this.sharePriceService  = sharePriceService;
        this.performanceService = performanceService;
    }

    /**
     * Render a chart for a single company.
     * Combines raw price data with SMA indicators.
     */
    public void renderChart(ShareQuery query) throws ServiceException {
        renderChart(query, new ArrayList<>());
    }

    /**
     * Render a chart for a single company with annotations.
     */
    public void renderChart(ShareQuery query, List<Annotation> annotations) throws ServiceException {
        System.out.println("[ChartingService] Rendering chart for: " + query.getSymbol());

        List<SharePrice>     prices    = sharePriceService.getPrices(query);
        PerformanceIndicator indicator = performanceService.calculate(query);

        System.out.println("  Symbol  : " + query.getSymbol());
        System.out.println("  Period  : " + query.getFrom() + " to " + query.getTo());
        System.out.println("  Records : " + prices.size());
        System.out.printf ("  Change  : %+.2f%%%n", indicator.getPercentChange());
        System.out.printf ("  52w High: %.2f%n",    indicator.getHigh52Week());
        System.out.printf ("  52w Low : %.2f%n",    indicator.getLow52Week());

        if (!indicator.getSma20().isEmpty()) {
            System.out.printf("  SMA(20) : %.2f (last)%n",
                    indicator.getSma20().get(indicator.getSma20().size() - 1));
        }

        // Print sample price rows
        System.out.printf("  %-12s %-10s %-10s %-10s %-10s%n",
                "Date", "Open", "High", "Low", "Close");
        System.out.println("  " + "-".repeat(55));
        prices.stream().limit(3).forEach(p ->
            System.out.printf("  %-12s %-10.2f %-10.2f %-10.2f %-10.2f%n",
                p.getDate(), p.getOpen(), p.getHigh(), p.getLow(), p.getClose()));
        if (prices.size() > 6) System.out.println("  ...");
        prices.stream().skip(Math.max(0, prices.size() - 3)).forEach(p ->
            System.out.printf("  %-12s %-10.2f %-10.2f %-10.2f %-10.2f%n",
                p.getDate(), p.getOpen(), p.getHigh(), p.getLow(), p.getClose()));

        // Show any annotations for this period
        annotations.stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(query.getSymbol()))
                .forEach(a -> System.out.println("  [Note " + a.getDate() + "] " + a.getNote()));
    }
}
