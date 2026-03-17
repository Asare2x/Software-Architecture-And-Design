package com.shareanalysis.service;

import com.shareanalysis.api.ShareDataProvider;
import com.shareanalysis.exception.DataProviderException;
import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;
import com.shareanalysis.repository.SharePriceRepository;

import java.util.List;

/**
 * Component: SharePriceService
 *
 * Central hub of the application — all other components connect through it.
 *
 * Connections (from the component diagram):
 *   Share Price         → SharePriceService  (raw price input)
 *   SharePriceRepository → SharePriceService  (stored data)
 *   ShareDataProvider   → SharePriceService  (live data)
 *   API                 ← SharePriceService  (exposes price endpoints)
 *   Price Comparison    ← SharePriceService  (needs price data)
 *   Alert Service       ← SharePriceService  (needs live data)
 *   Performance Service ← SharePriceService  (needs analytics data)
 *   Charting Service    ← SharePriceService  (needs price data)
 *   Annotation Component → SharePriceService  (metadata)
 *
 * Retrieval strategy: cache-first with API fallback.
 */
public class SharePriceService {

    private final ShareDataProvider    dataProvider;
    private final SharePriceRepository repository;

    public SharePriceService(ShareDataProvider dataProvider, SharePriceRepository repository) {
        this.dataProvider = dataProvider;
        this.repository   = repository;
    }

    /**
     * Retrieve daily prices for a single company.
     * Checks local cache first; calls external API if not cached.
     */
    public List<SharePrice> getPrices(ShareQuery query) throws ServiceException {
        System.out.println("[SharePriceService] Query: " + query);

        // Cache-first
        if (repository.hasCachedData(query)) {
            System.out.println("[SharePriceService] Cache hit.");
            return repository.load(query);
        }

        // Fallback to external API
        if (!dataProvider.isAvailable()) {
            throw new ServiceException("No cached data and external provider is unreachable for: " + query);
        }

        try {
            List<SharePrice> prices = dataProvider.fetchPrices(query);
            repository.save(prices);
            return prices;
        } catch (DataProviderException e) {
            throw new ServiceException("Failed to fetch data from external provider.", e);
        }
    }

    /**
     * Calculate the Simple Moving Average over a given window.
     * Used by PerformanceService and ChartingService.
     */
    public List<Double> calculateSMA(List<SharePrice> prices, int windowDays) {
        java.util.List<Double> sma = new java.util.ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            int start = Math.max(0, i - windowDays + 1);
            double avg = prices.subList(start, i + 1).stream()
                    .mapToDouble(SharePrice::getClose)
                    .average().orElse(0.0);
            sma.add(avg);
        }
        return sma;
    }
}
