package com.shareanalysis.service;

import com.shareanalysis.api.DataProviderException;
import com.shareanalysis.api.ShareDataProvider;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;
import com.shareanalysis.repository.ShareRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Concrete implementation of {@link SharePriceService}.
 *
 * Orchestration strategy (cache-first with network fallback):
 *  1. If the local cache can satisfy the query → return cached data.
 *  2. If the external provider is available → fetch, cache, return.
 *  3. If both fail → throw {@link ServiceException}.
 *
 * This strategy fulfils the requirement for offline functionality.
 *
 * Architecture role: Service / Business Logic Layer
 */
public class SharePriceServiceImpl implements SharePriceService {

    private final ShareDataProvider dataProvider;
    private final ShareRepository   repository;

    /**
     * Constructor injection keeps this class testable and decoupled.
     */
    public SharePriceServiceImpl(ShareDataProvider dataProvider, ShareRepository repository) {
        this.dataProvider = dataProvider;
        this.repository   = repository;
    }

    /**
     * {@inheritDoc}
     *
     * Implements the cache-first retrieval strategy.
     */
    @Override
    public List<SharePrice> getPrices(ShareQuery query) throws ServiceException {
        System.out.println("[SharePriceService] Handling query: " + query);

        // 1. Try local cache first
        if (repository.hasCachedData(query)) {
            System.out.println("[SharePriceService] Cache hit — returning local data.");
            return repository.load(query);
        }

        // 2. Try external provider
        if (!dataProvider.isAvailable()) {
            throw new ServiceException(
                    "No cached data available and external provider is unreachable for: " + query);
        }

        try {
            List<SharePrice> prices = dataProvider.fetchPrices(query);
            repository.save(prices);           // persist for offline use
            return prices;
        } catch (DataProviderException e) {
            throw new ServiceException("Failed to fetch data from external provider.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<SharePrice>> comparePrices(ShareQuery queryA, ShareQuery queryB)
            throws ServiceException {
        Map<String, List<SharePrice>> result = new LinkedHashMap<>();
        result.put(queryA.getSymbol(), getPrices(queryA));
        result.put(queryB.getSymbol(), getPrices(queryB));
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * Simple Moving Average: for each index i, average the previous
     * {@code windowDays} closing prices (or fewer if near the start).
     */
    @Override
    public List<Double> calculateSMA(List<SharePrice> prices, int windowDays) {
        List<Double> sma = new ArrayList<>(prices.size());
        for (int i = 0; i < prices.size(); i++) {
            int start = Math.max(0, i - windowDays + 1);
            double avg = prices.subList(start, i + 1).stream()
                    .mapToDouble(SharePrice::getClose)
                    .average()
                    .orElse(0.0);
            sma.add(avg);
        }
        return sma;
    }
}
