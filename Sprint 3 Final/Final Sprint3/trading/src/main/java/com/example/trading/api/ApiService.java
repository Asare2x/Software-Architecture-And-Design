package com.example.trading.api;

import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.util.List;

/**
 * Convenience facade over IAPI.
 * Other classes can inject ApiService instead of depending directly on
 * the concrete YahooFinanceProvider, keeping the dependency pointing inward.
 */
public class ApiService {

    private final IAPI api;

    public ApiService(IAPI api) {
        this.api = api;
    }

    public List<SharePrice> fetchPrices(ShareQuery query) throws DataProviderException {
        try {
            return api.getSharePrices(query);
        } catch (DataProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new DataProviderException("Unexpected error from API: " + e.getMessage(), e);
        }
    }

    public boolean isProviderAvailable() {
        return api.isAvailable();
    }

    public String getProviderName() {
        return api.getProviderName();
    }
}
