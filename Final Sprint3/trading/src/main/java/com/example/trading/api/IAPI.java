package com.example.trading.api;

import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.util.List;

/**
 * Contract for any external market data API.
 * Implemented by YahooFinanceProvider (and any future providers).
 * ShareDataProvider depends on this interface to fetch live data.
 */
public interface IAPI {
    List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException;
    boolean isAvailable();
    String  getProviderName();
}
