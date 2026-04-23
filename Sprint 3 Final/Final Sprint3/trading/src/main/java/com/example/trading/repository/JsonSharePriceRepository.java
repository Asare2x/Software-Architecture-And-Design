package com.example.trading.repository;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JsonSharePriceRepository implements SharePriceRepository, com.example.trading.api.IChartData {
    
    private final Map<String, List<SharePrice>> priceData = new ConcurrentHashMap<>();
    
    @Override
    public SharePrice save(SharePrice sharePrice) {
        if (sharePrice == null) {
            throw new IllegalArgumentException("SharePrice cannot be null");
        }
        if (sharePrice.getSymbol() == null || sharePrice.getSymbol().trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        String symbol = sharePrice.getSymbol().toUpperCase();
        priceData.computeIfAbsent(symbol, k -> new ArrayList<>()).add(sharePrice);
        
        // Keep data sorted by date (most recent first)
        priceData.get(symbol).sort((a, b) -> b.getDate().compareTo(a.getDate()));
        
        return sharePrice;
    }
    
    @Override
    public Optional<SharePrice> findLatestBySymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return Optional.empty();
        }
        
        List<SharePrice> prices = priceData.get(symbol.toUpperCase());
        if (prices == null || prices.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(prices.get(0)); // First element is most recent
    }
    
    @Override
    public List<SharePrice> findBySymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SharePrice> prices = priceData.get(symbol.toUpperCase());
        return prices != null ? new ArrayList<>(prices) : new ArrayList<>();
    }
    
    @Override
    public List<SharePrice> findBySymbolAndDateRange(String symbol, LocalDate startDate, LocalDate endDate) {
        if (symbol == null || symbol.trim().isEmpty() || startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        
        return findBySymbol(symbol).stream()
                .filter(price -> !price.getDate().isBefore(startDate) && 
                               !price.getDate().isAfter(endDate))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<SharePrice> findBySymbolAndDateRange(String symbol, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (symbol == null || symbol.trim().isEmpty() || startDateTime == null || endDateTime == null) {
            return new ArrayList<>();
        }
        
        return findBySymbol(symbol).stream()
                .filter(price -> {
                    LocalDateTime priceDateTime = price.getTimestamp();
                    return !priceDateTime.isBefore(startDateTime) && !priceDateTime.isAfter(endDateTime);
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<String> findAllSymbols() {
        return new ArrayList<>(priceData.keySet());
    }
    
    @Override
    public void deleteBySymbol(String symbol) {
        if (symbol != null) {
            priceData.remove(symbol.toUpperCase());
        }
    }
    
    @Override
    public void deleteOldData(LocalDateTime beforeDate) {
        if (beforeDate == null) {
            return;
        }
        
        priceData.values().forEach(prices -> 
            prices.removeIf(price -> price.getTimestamp().isBefore(beforeDate))
        );
        
        // Remove empty symbol entries
        priceData.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    @Override
    public long count() {
        return priceData.values().stream()
                .mapToLong(List::size)
                .sum();
    }
    
    @Override
    public long countBySymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            return 0;
        }
        
        List<SharePrice> prices = priceData.get(symbol.toUpperCase());
        return prices != null ? prices.size() : 0;
    }
    
    /**
     * Clear all data (useful for testing)
     */
    public void clear() {
        priceData.clear();
    }
    
    /**
     * Load sample data for testing
     */
    public void loadSampleData() {
        // Add some sample data for common symbols
        String[] symbols = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN"};
        Random random = new Random();
        
        for (String symbol : symbols) {
            for (int i = 30; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                
                double basePrice = 100 + random.nextDouble() * 400; // Random price between 100-500
                BigDecimal openPrice = BigDecimal.valueOf(basePrice);
                BigDecimal highPrice = BigDecimal.valueOf(basePrice + random.nextDouble() * 10);
                BigDecimal lowPrice = BigDecimal.valueOf(basePrice - random.nextDouble() * 10);
                BigDecimal closePrice = BigDecimal.valueOf(basePrice + (random.nextDouble() - 0.5) * 5);
                long volume = (long) (1000000 + random.nextDouble() * 5000000);
                
                SharePrice price = new SharePrice(symbol, date, openPrice, closePrice, highPrice, lowPrice, volume);
                save(price);
            }
        }
    }

    // ── IChartData implementation ─────────────────────────────────────────

    @Override
    public java.util.List<SharePrice> getChartData(ShareQuery query) {
        return findBySymbolAndDateRange(query.getSymbol(),
                query.getStartDate(), query.getEndDate());
    }

    @Override
    public java.util.List<String> getAvailableSymbols() {
        return findAllSymbols();
    }
}
