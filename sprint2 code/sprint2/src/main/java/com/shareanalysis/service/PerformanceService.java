package com.shareanalysis.service;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.PerformanceIndicator;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;

/**
 * Component: Performance Service
 * Directions:
 *   Performance Service ← SharePriceService  (needs analytics data)
 *   Charting Service    ← Performance Service (provides indicators to chart)
 *
 * Computes analytical indicators: SMA, % change, 52-week high/low.
 */
public class PerformanceService {

    private final SharePriceService sharePriceService;

    public PerformanceService(SharePriceService sharePriceService) {
        this.sharePriceService = sharePriceService;
    }

    /**
     * Calculate performance indicators for a given query.
     * Result is consumed by ChartingService to overlay on the chart.
     */
    public PerformanceIndicator calculate(ShareQuery query) throws ServiceException {
        System.out.println("[PerformanceService] Calculating indicators for: " + query.getSymbol());

        List<SharePrice> prices = sharePriceService.getPrices(query);

        List<Double> sma20    = sharePriceService.calculateSMA(prices, 20);
        double percentChange  = calcPercentChange(prices);
        double high52Week     = prices.stream().mapToDouble(SharePrice::getHigh).max().orElse(0);
        double low52Week      = prices.stream().mapToDouble(SharePrice::getLow).min().orElse(0);

        PerformanceIndicator indicator = new PerformanceIndicator(
                query.getSymbol(), sma20, percentChange, high52Week, low52Week);

        System.out.println("[PerformanceService] " + indicator);
        return indicator;
    }

    private double calcPercentChange(List<SharePrice> prices) {
        if (prices == null || prices.size() < 2) return 0.0;
        double first = prices.get(0).getClose();
        double last  = prices.get(prices.size() - 1).getClose();
        return first == 0 ? 0.0 : ((last - first) / first) * 100.0;
    }
}
