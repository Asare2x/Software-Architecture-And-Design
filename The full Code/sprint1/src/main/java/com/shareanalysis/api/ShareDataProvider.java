package com.shareanalysis.api;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;

/**
 * Component Interface: ShareDataProvider
 *
 * Defines the contract for any external share-price data source.
 * Implementations could target Yahoo Finance, Alpha Vantage, etc.
 *
 * This abstraction is central to the Simple Architecture principle:
 * higher-level components depend on this interface, not on a concrete
 * vendor API, making it straightforward to swap data providers.
 */
public interface ShareDataProvider {

    /**
     * Fetch daily share price data from an external source.
     *
     * @param query the query describing symbol and date range
     * @return ordered list of {@link SharePrice} objects (oldest first)
     * @throws DataProviderException if the external API call fails
     */
    List<SharePrice> fetchPrices(ShareQuery query) throws DataProviderException;

    /**
     * Check whether the external data source is reachable.
     *
     * @return true if a network connection to the provider is available
     */
    boolean isAvailable();
}
