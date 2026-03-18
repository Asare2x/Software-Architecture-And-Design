package com.shareanalysis.service;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;

/**
 * Component: Price Comparison
 * Direction: Price Comparison ← SharePriceService (needs price data)
 *
 * Retrieves price data for two companies and compares their performance
 * over a shared date range.
 */
public class PriceComparisonService {

    private final SharePriceService sharePriceService;

    public PriceComparisonService(SharePriceService sharePriceService) {
        this.sharePriceService = sharePriceService;
    }

    /**
     * Compare two companies over the same date range.
     *
     * @return a formatted summary string for display
     */
    public String compare(ShareQuery queryA, ShareQuery queryB) throws ServiceException {
        System.out.println("[PriceComparisonService] Comparing: "
                + queryA.getSymbol() + " vs " + queryB.getSymbol());

        List<SharePrice> pricesA = sharePriceService.getPrices(queryA);
        List<SharePrice> pricesB = sharePriceService.getPrices(queryB);

        double changeA = percentChange(pricesA);
        double changeB = percentChange(pricesB);

        String winner = changeA >= changeB ? queryA.getSymbol() : queryB.getSymbol();

        return String.format(
            "Comparison: %s (%+.2f%%) vs %s (%+.2f%%) | Better performer: %s",
            queryA.getSymbol(), changeA, queryB.getSymbol(), changeB, winner);
    }

    private double percentChange(List<SharePrice> prices) {
        if (prices == null || prices.size() < 2) return 0.0;
        double first = prices.get(0).getClose();
        double last  = prices.get(prices.size() - 1).getClose();
        return first == 0 ? 0.0 : ((last - first) / first) * 100.0;
    }
}
