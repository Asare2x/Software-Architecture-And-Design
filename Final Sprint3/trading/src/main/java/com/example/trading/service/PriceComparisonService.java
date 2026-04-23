package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceComparisonService {
    private final IPriceService sharePriceService;
    
    public PriceComparisonService(IPriceService sharePriceService) {
        this.sharePriceService = sharePriceService;
    }
    
    /**
     * Compare performance of two symbols over a date range
     */
    public ComparisonResult comparePerformance(String symbol1, String symbol2, 
                                             LocalDate startDate, LocalDate endDate) {
        try {
            List<SharePrice> prices1 = sharePriceService.getSharePrices(
                new ShareQuery(symbol1, startDate, endDate));
            List<SharePrice> prices2 = sharePriceService.getSharePrices(
                new ShareQuery(symbol2, startDate, endDate));
            
            if (prices1.isEmpty() || prices2.isEmpty()) {
                throw new ServiceException("Insufficient data for comparison");
            }
            
            ComparisonResult result = new ComparisonResult();
            result.symbol1 = symbol1;
            result.symbol2 = symbol2;
            result.startDate = startDate;
            result.endDate = endDate;
            
            // Calculate performance metrics
            result.performance1 = calculatePerformance(prices1);
            result.performance2 = calculatePerformance(prices2);
            
            // Calculate relative performance
            result.relativePerformance = result.performance1.subtract(result.performance2);
            
            return result;
            
        } catch (Exception e) {
            throw new ServiceException("Error comparing performance", e);
        }
    }
    
    /**
     * Calculate percentage change over the period
     */
    private BigDecimal calculatePerformance(List<SharePrice> prices) {
        if (prices.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Sort by date to ensure we have start and end prices
        prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
        
        BigDecimal startPrice = prices.get(0).getClosePrice();
        BigDecimal endPrice = prices.get(prices.size() - 1).getClosePrice();
        
        if (startPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return endPrice.subtract(startPrice)
                      .divide(startPrice, 4, RoundingMode.HALF_UP)
                      .multiply(new BigDecimal("100"));
    }
    
    /**
     * Get correlation between two symbols
     */
    public BigDecimal getCorrelation(String symbol1, String symbol2, LocalDate startDate, LocalDate endDate) {
        try {
            List<SharePrice> prices1 = sharePriceService.getSharePrices(
                new ShareQuery(symbol1, startDate, endDate));
            List<SharePrice> prices2 = sharePriceService.getSharePrices(
                new ShareQuery(symbol2, startDate, endDate));
            
            if (prices1.size() != prices2.size()) {
                throw new ServiceException("Mismatched data for correlation calculation");
            }
            
            // Simple correlation calculation (Pearson correlation coefficient)
            return calculateCorrelation(prices1, prices2);
            
        } catch (Exception e) {
            throw new ServiceException("Error calculating correlation", e);
        }
    }
    
    private BigDecimal calculateCorrelation(List<SharePrice> prices1, List<SharePrice> prices2) {
        // Simplified correlation calculation
        // In a real implementation, you'd use proper statistical libraries
        return BigDecimal.valueOf(0.5); // Placeholder
    }
    
    /**
     * Result class for comparison operations
     */
    public static class ComparisonResult {
        public String symbol1;
        public String symbol2;
        public LocalDate startDate;
        public LocalDate endDate;
        public BigDecimal performance1;
        public BigDecimal performance2;
        public BigDecimal relativePerformance;
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("symbol1", symbol1);
            map.put("symbol2", symbol2);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            map.put("performance1", performance1);
            map.put("performance2", performance2);
            map.put("relativePerformance", relativePerformance);
            return map;
        }
    }
}
