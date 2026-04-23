package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.util.List;

/**
 * Price service contract — used by the UI (TradingController) and ChartingService.
 * Implemented by SharePriceService.
 */
public interface IPriceService {
    List<SharePrice> getSharePrices(ShareQuery query) throws ServiceException;
    SharePrice       getLatestPrice(String symbol)    throws ServiceException;
    List<String>     getAvailableSymbols();
    boolean          isDataProviderAvailable();
}
