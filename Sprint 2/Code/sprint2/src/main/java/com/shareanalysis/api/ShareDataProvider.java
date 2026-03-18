package com.shareanalysis.api;

import com.shareanalysis.exception.DataProviderException;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.List;

/**
 * Component: ShareDataProvider
 * Direction: ShareDataProvider → SharePriceService (live data provider)
 *
 * Interface for any external share price data source.
 * Concrete implementations (Yahoo Finance, Alpha Vantage) plug in here.
 */
public interface ShareDataProvider {

    /**
     * Fetch daily OHLCV prices from the external source.
     *
     * @param query the symbol and date range to fetch
     * @return ordered list of prices, oldest first
     * @throws DataProviderException if the API call fails
     */
    List<SharePrice> fetchPrices(ShareQuery query) throws DataProviderException;

    /** Returns true if the external data source is reachable. */
    boolean isAvailable();
}
