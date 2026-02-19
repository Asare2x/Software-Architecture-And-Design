package com.example.demo1.service;

import com.example.demo1.model.SharePrice;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for fetching share price data from an external source.
 * Supports Clean Architecture by decoupling the data-fetching logic from the UI.
 */
public interface ShareDataService {

    /**
     * Fetches daily share prices for a given stock symbol between two dates.
     * Maximum date range is 2 years.
     *
     * @param symbol    the stock ticker symbol (e.g. "AAPL")
     * @param startDate the start of the date range (inclusive)
     * @param endDate   the end of the date range (inclusive)
     * @return a list of SharePrice records, ordered by date ascending
     * @throws IllegalArgumentException if the date range exceeds 2 years
     */
    List<SharePrice> fetchPrices(String symbol, LocalDate startDate, LocalDate endDate);
}
