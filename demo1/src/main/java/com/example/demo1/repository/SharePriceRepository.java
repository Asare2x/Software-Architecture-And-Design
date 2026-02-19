package com.example.demo1.repository;

import com.example.demo1.model.SharePrice;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for persisting and retrieving share price data locally.
 * Enables offline functionality by caching fetched data.
 */
public interface SharePriceRepository {

    /**
     * Saves a list of share prices to persistent storage.
     *
     * @param prices the list of SharePrice records to save
     */
    void savePrices(List<SharePrice> prices);

    /**
     * Retrieves cached share prices for a symbol within a date range.
     *
     * @param symbol    the stock ticker symbol
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return a list of cached SharePrice records, or empty list if none found
     */
    List<SharePrice> getPrices(String symbol, LocalDate startDate, LocalDate endDate);

    /**
     * Checks whether data for a given symbol and date range is already cached.
     *
     * @param symbol    the stock ticker symbol
     * @param startDate start of the date range
     * @param endDate   end of the date range
     * @return true if cached data exists, false otherwise
     */
    boolean hasData(String symbol, LocalDate startDate, LocalDate endDate);
}
