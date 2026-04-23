package com.example.trading.repository;

import com.example.trading.model.SharePrice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SharePriceRepository {
    /**
     * Save a share price record
     * @param sharePrice the share price to save
     * @return the saved share price
     */
    SharePrice save(SharePrice sharePrice);
    
    /**
     * Find the latest price for a symbol
     * @param symbol the stock symbol
     * @return Optional containing the latest price if found
     */
    Optional<SharePrice> findLatestBySymbol(String symbol);
    
    /**
     * Find all prices for a symbol
     * @param symbol the stock symbol
     * @return list of all prices for the symbol
     */
    List<SharePrice> findBySymbol(String symbol);
    
    /**
     * Find prices for a symbol within a date range using LocalDate
     * @param symbol the stock symbol
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of prices within the date range
     */
    List<SharePrice> findBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find prices for a symbol within a date range using LocalDateTime
     * @param symbol the stock symbol
     * @param startDateTime start of date range
     * @param endDateTime end of date range
     * @return list of prices within the date range
     */
    List<SharePrice> findBySymbolAndDateRange(String symbol, LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * Find all unique symbols
     * @return list of all symbols that have price data
     */
    List<String> findAllSymbols();
    
    /**
     * Delete all prices for a symbol
     * @param symbol the stock symbol
     */
    void deleteBySymbol(String symbol);
    
    /**
     * Delete old price data before a certain date
     * @param beforeDate the cutoff date
     */
    void deleteOldData(LocalDateTime beforeDate);
    
    /**
     * Count total number of price records
     * @return total count of price records
     */
    long count();
    
    /**
     * Count price records for a specific symbol
     * @param symbol the stock symbol
     * @return count of price records for the symbol
     */
    long countBySymbol(String symbol);
}
