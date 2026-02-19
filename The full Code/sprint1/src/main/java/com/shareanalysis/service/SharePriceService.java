package com.shareanalysis.service;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;
import java.util.Map;

/**
 * Component Interface: SharePriceService
 *
 * Defines the business-logic operations available to the UI layer.
 * The service layer sits between the UI and the data/API layers,
 * orchestrating caching, retrieval, and any future analytical computations.
 *
 * All UI components interact with this interface, never directly with
 * repositories or data providers.
 */
public interface SharePriceService {

    /**
     * Retrieve daily share prices for the given query.
     *
     * The implementation should:
     *  1. Check local cache first (offline support).
     *  2. Fetch from the external provider if cache is stale or empty.
     *  3. Persist new data to the local cache before returning.
     *
     * @param query the share symbol and date range to query
     * @return ordered list of daily prices (oldest first)
     * @throws ServiceException if data cannot be retrieved from any source
     */
    List<SharePrice> getPrices(ShareQuery query) throws ServiceException;

    /**
     * Retrieve prices for two symbols in one call, used for comparison charts.
     *
     * @param queryA first company's query
     * @param queryB second company's query
     * @return map keyed by ticker symbol, each value is the price list
     * @throws ServiceException if either query fails
     */
    Map<String, List<SharePrice>> comparePrices(ShareQuery queryA, ShareQuery queryB)
            throws ServiceException;

    /**
     * Calculate the simple moving average (SMA) over a given window.
     *
     * Sprint 1: Defined here for architectural completeness.
     * Sprint 2/3: Full implementation with technical analysis features.
     *
     * @param prices the raw daily prices
     * @param windowDays number of days in the moving-average window
     * @return list of SMA values aligned to the input list
     */
    List<Double> calculateSMA(List<SharePrice> prices, int windowDays);
}
