package com.example.trading.api;

/**
 * Central data provider interface — extends both IAPI and IDataProvider.
 * YahooFinanceProvider implements this interface, satisfying all three contracts.
 * SharePriceService depends on this interface to retrieve prices.
 */
public interface ShareDataProvider extends IAPI, IDataProvider {
    // Inherits from IAPI:       getSharePrices(), isAvailable(), getProviderName()
    // Inherits from IDataProvider: getSharePrices(), isAvailable()
}
