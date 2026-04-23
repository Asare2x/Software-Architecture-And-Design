package com.example.trading.service;

import com.example.trading.soa.TradingServiceContract;
import com.example.trading.soa.ServiceHealth;
import com.example.trading.soa.ServiceMetadata;
import java.util.concurrent.CompletableFuture;
import com.example.trading.api.ShareDataProvider;
import com.example.trading.exception.DataProviderException;
import com.example.trading.exception.ServiceException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.repository.SharePriceRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

public class SharePriceService implements IPriceService, TradingServiceContract {
    private final SharePriceRepository repository;
    private final ShareDataProvider dataProvider;
    
    public SharePriceService(SharePriceRepository repository, ShareDataProvider dataProvider) {
        if (repository == null) {
            throw new IllegalArgumentException("Repository cannot be null");
        }
        if (dataProvider == null) {
            throw new IllegalArgumentException("Data provider cannot be null");
        }
        this.repository = repository;
        this.dataProvider = dataProvider;
    }
    
    /**
     * Get share prices for a symbol between dates
     * First checks local storage, then fetches from external API if needed
     */
    public List<SharePrice> getSharePrices(ShareQuery query) {
        validateQuery(query);
        
        try {
            // Convert LocalDate to LocalDateTime for repository calls
            LocalDateTime startDateTime = query.getStartDate().atStartOfDay();
            LocalDateTime endDateTime = query.getEndDate().atTime(LocalTime.MAX);
            
            // First check if we have the data locally
            List<SharePrice> localData = repository.findBySymbolAndDateRange(
                query.getSymbol(), startDateTime, endDateTime);
            
            // If we have complete data locally, return it sorted
            if (isDataComplete(localData, query)) {
                return sortByDate(localData);
            }
            
            // Otherwise fetch from external provider
            List<SharePrice> externalData;
            try {
                externalData = dataProvider.getSharePrices(query);
            } catch (DataProviderException e) {
                return handleDataProviderFailure(query, localData, e);
            }
            
            // Validate and save the new data to local storage
            if (externalData != null && !externalData.isEmpty()) {
                for (SharePrice price : externalData) {
                    if (price != null) {
                        repository.save(price);
                    }
                }
                return sortByDate(externalData);
            }
            
            // If external data is empty but we have some local data, return it
            if (!localData.isEmpty()) {
                return sortByDate(localData);
            }
            
            throw new ServiceException("No data available for symbol: " + query.getSymbol());
            
        } catch (ServiceException e) {
            throw e; // Re-throw service exceptions
        } catch (Exception e) {
            throw new ServiceException("Error retrieving share prices for " + query.getSymbol(), e);
        }
    }
    
    /**
     * Get the latest price for a symbol
     */
    public SharePrice getLatestPrice(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        
        try {
            // Check repository first - using Optional properly
            SharePrice latest = repository.findLatestBySymbol(normalizedSymbol).orElse(null);
            
            // If we have today's data, return it
            if (latest != null && latest.getDate().isEqual(LocalDate.now())) {
                return latest;
            }
            
            // Fetch latest from external provider
            ShareQuery query = new ShareQuery(normalizedSymbol, LocalDate.now(), LocalDate.now());
            try {
                List<SharePrice> prices = dataProvider.getSharePrices(query);
                
                if (prices != null && !prices.isEmpty()) {
                    SharePrice latestPrice = prices.get(0);
                    repository.save(latestPrice);
                    return latestPrice;
                }
            } catch (DataProviderException e) {
                // Log the error but continue to return cached data if available
                System.err.println("Data provider failed for " + normalizedSymbol + ": " + e.getMessage());
            }
            
            // Return cached data if external fails
            if (latest != null) {
                return latest;
            }
            
            throw new ServiceException("No price data available for symbol: " + normalizedSymbol);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error getting latest price for " + normalizedSymbol, e);
        }
    }
    
    /**
     * Check if we have complete data for the requested range
     */
    private boolean isDataComplete(List<SharePrice> localData, ShareQuery query) {
        if (localData == null || localData.isEmpty()) {
            return false;
        }
        
        // Get all dates in the data
        List<LocalDate> availableDates = localData.stream()
            .map(SharePrice::getDate)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
            
        // Check if we have data for start and end dates
        if (!availableDates.contains(query.getStartDate()) || 
            !availableDates.contains(query.getEndDate())) {
            return false;
        }
        
        // For a more thorough check, ensure we don't have large gaps
        // This is a simple heuristic - in production you might want more sophisticated logic
        long daysBetween = query.getStartDate().until(query.getEndDate()).getDays();
        long expectedWorkingDays = Math.max(1, daysBetween * 5 / 7); // Rough estimate excluding weekends
        
        return availableDates.size() >= Math.min(expectedWorkingDays * 0.8, daysBetween + 1);
    }
    
    /**
     * Handle data provider failure by returning local data if available
     */
    private List<SharePrice> handleDataProviderFailure(ShareQuery query, List<SharePrice> localData, DataProviderException e) {
        if (localData != null && !localData.isEmpty()) {
            System.err.println("Data provider failed, using cached data for " + query.getSymbol() + ": " + e.getMessage());
            return sortByDate(localData);
        }
        throw new ServiceException("No data available for " + query.getSymbol() + " (external provider failed)", e);
    }
    
    /**
     * Sort share prices by date in ascending order
     */
    private List<SharePrice> sortByDate(List<SharePrice> prices) {
        if (prices == null || prices.isEmpty()) {
            return prices;
        }
        return prices.stream()
                .sorted((p1, p2) -> p1.getDate().compareTo(p2.getDate()))
                .collect(Collectors.toList());
    }
    
    /**
     * Validate query parameters
     */
    private void validateQuery(ShareQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("Query cannot be null");
        }
        if (!query.isValid()) {
            throw new IllegalArgumentException("Invalid query: " + query);
        }
        if (query.getSymbol() == null || query.getSymbol().trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (query.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (query.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (query.getStartDate().isAfter(query.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        // Validate symbol format (basic validation)
        String normalizedSymbol = query.getSymbol().trim().toUpperCase();
        if (!normalizedSymbol.matches("^[A-Z]{1,10}$")) {
            throw new IllegalArgumentException("Invalid symbol format: " + query.getSymbol());
        }
    }
    
    /**
     * Get all available symbols in the system
     */
    public List<String> getAvailableSymbols() {
        try {
            List<String> symbols = repository.findAllSymbols();
            return symbols != null ? symbols : List.of();
        } catch (Exception e) {
            throw new ServiceException("Error retrieving available symbols", e);
        }
    }
    
    /**
     * Clear cache for a specific symbol
     */
    public void clearCache(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        try {
            repository.deleteBySymbol(symbol.trim().toUpperCase());
        } catch (Exception e) {
            throw new ServiceException("Error clearing cache for symbol: " + symbol, e);
        }
    }
    
    /**
     * Check if data provider is available
     */
    public boolean isDataProviderAvailable() {
        try {
            return dataProvider.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get cache statistics for a symbol
     */
    public long getCacheSize(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return 0;
        }
        try {
            return repository.countBySymbol(symbol.trim().toUpperCase());
        } catch (Exception e) {
            return 0;
        }
    }

    // ── TradingServiceContract implementation ──────────────────────────────

    @Override
    public CompletableFuture<List<SharePrice>> getStockPricesAsync(ShareQuery query) {
        return CompletableFuture.supplyAsync(() -> getSharePrices(query));
    }

    @Override
    public List<SharePrice> getStockPrices(ShareQuery query) {
        return getSharePrices(query);
    }

    @Override
    public boolean isValidSymbol(String symbol) {
        return symbol != null && !symbol.trim().isEmpty()
                && symbol.trim().length() <= 5
                && symbol.trim().matches("[A-Za-z.\\-]+");
    }

    @Override
    public ServiceHealth getHealthStatus() {
        if (isDataProviderAvailable()) {
            return ServiceHealth.healthy("SharePriceService");
        }
        return ServiceHealth.degraded("SharePriceService", "Data provider unavailable", 0);
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return ServiceMetadata.createTradingService("1.0");
    }
}
