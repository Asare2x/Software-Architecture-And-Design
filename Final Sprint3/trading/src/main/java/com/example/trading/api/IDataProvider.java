package com.example.trading.api;

import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.util.List;

/**
 * Internal data provider contract used by SharePriceService.
 * Abstracts where the data comes from (live API, cache, or local store).
 * Implemented by ShareDataProvider (which itself uses IAPI for live data).
 */
public interface IDataProvider {
    List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException;
    boolean isAvailable();
}
