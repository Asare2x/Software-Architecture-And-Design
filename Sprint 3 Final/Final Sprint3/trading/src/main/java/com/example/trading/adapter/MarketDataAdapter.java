package com.example.trading.adapter;

import com.example.trading.api.IAPI;
import com.example.trading.api.IDataProvider;
import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.util.Collections;
import java.util.List;

/**
 * ADAPTER PATTERN
 *
 * Adapts any external IAPI implementation into the internal IDataProvider
 * contract expected by SharePriceService and the pipeline layer.
 *
 * This means we can swap Yahoo Finance for any other provider
 * (Alpha Vantage, Polygon.io, etc.) by simply creating a new IAPI
 * implementation and wrapping it in this adapter — nothing else changes.
 *
 * Architecture role: Integration Layer → Business Layer adapter.
 */
public class MarketDataAdapter implements IDataProvider {

    private final IAPI externalApi;
    private final String adapterName;

    public MarketDataAdapter(IAPI externalApi) {
        this.externalApi = externalApi;
        this.adapterName = "MarketDataAdapter[" + externalApi.getProviderName() + "]";
    }

    /**
     * Adapts the external IAPI.getSharePrices() call to the IDataProvider contract.
     * Handles exceptions from the external API and wraps them in DataProviderException.
     */
    @Override
    public List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException {
        if (query == null) {
            throw new DataProviderException("ShareQuery cannot be null");
        }
        if (!externalApi.isAvailable()) {
            throw new DataProviderException(externalApi.getProviderName() + " is not available");
        }
        try {
            List<SharePrice> prices = externalApi.getSharePrices(query);
            return prices != null ? prices : Collections.emptyList();
        } catch (DataProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new DataProviderException(
                adapterName + " failed to retrieve data: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        return externalApi.isAvailable();
    }

    public String getAdapterName() {
        return adapterName;
    }

    public String getWrappedProviderName() {
        return externalApi.getProviderName();
    }
}
