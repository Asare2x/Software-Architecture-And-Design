package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.PerformanceIndicator;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PerformanceService implements IPerformance {
    private final IPriceService sharePriceService;
    
    public PerformanceService(IPriceService sharePriceService) {
        if (sharePriceService == null) {
            throw new IllegalArgumentException("SharePriceService cannot be null");
        }
        this.sharePriceService = sharePriceService;
    }
    
    /**
     * Calculate technical indicators for a symbol
     */
    public List<PerformanceIndicator> calculateIndicators(String symbol, LocalDate startDate, LocalDate endDate) {
        validateInputs(symbol, startDate, endDate);
        
        try {
            // Get extended date range for accurate calculations
            LocalDate extendedStart = startDate.minusDays(60);
            List<SharePrice> prices = sharePriceService.getSharePrices(
                new ShareQuery(symbol, extendedStart, endDate));
            
            if (prices.size() < 50) {
                throw new ServiceException("Insufficient data for technical analysis. Need at least 50 data points, got " + prices.size());
            }
            
            // Sort by date
            prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
            
            List<PerformanceIndicator> indicators = new ArrayList<>();
            
            for (int i = 50; i < prices.size(); i++) {
                SharePrice currentPrice = prices.get(i);
                
                // Only calculate for dates within our requested range
                if (currentPrice.getDate().isBefore(startDate)) {
                    continue;
                }
                
                PerformanceIndicator indicator = new PerformanceIndicator(symbol, currentPrice.getDate());
                
                // Calculate various indicators
                indicator.setSma20(calculateSMA(prices, i, 20));
                indicator.setSma50(calculateSMA(prices, i, 50));
                indicator.setEma12(calculateEMA(prices, i, 12));
                indicator.setEma26(calculateEMA(prices, i, 26));
                indicator.setRsi(calculateRSI(prices, i, 14));
                
                // Calculate MACD (check for null values)
                if (indicator.getEma12() != null && indicator.getEma26() != null) {
                    BigDecimal macd = indicator.getEma12().subtract(indicator.getEma26());
                    indicator.setMacd(macd);
                }
                
                indicators.add(indicator);
            }
            
            return indicators;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error calculating performance indicators for " + symbol, e);
        }
    }
    
    /**
     * Calculate Simple Moving Average
     */
    private BigDecimal calculateSMA(List<SharePrice> prices, int currentIndex, int period) {
        if (currentIndex < period - 1 || prices == null || prices.isEmpty()) {
            return null;
        }
        
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
            BigDecimal price = prices.get(i).getClosePrice();
            if (price != null) {
                sum = sum.add(price);
            }
        }
        
        return sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Exponential Moving Average
     */
    private BigDecimal calculateEMA(List<SharePrice> prices, int currentIndex, int period) {
        if (currentIndex < period - 1 || prices == null || prices.isEmpty()) {
            return null;
        }
        
        // Calculate smoothing factor
        BigDecimal multiplier = BigDecimal.valueOf(2.0).divide(BigDecimal.valueOf(period + 1), 10, RoundingMode.HALF_UP);
        
        // Start with SMA for the first EMA value
        BigDecimal ema = calculateSMA(prices, currentIndex - period + period, period);
        if (ema == null) {
            return null;
        }
        
        // Calculate EMA iteratively
        for (int i = currentIndex - period + period + 1; i <= currentIndex; i++) {
            BigDecimal currentPrice = prices.get(i).getClosePrice();
            if (currentPrice != null) {
                ema = currentPrice.multiply(multiplier)
                        .add(ema.multiply(BigDecimal.ONE.subtract(multiplier)));
            }
        }
        
        return ema.setScale(4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Relative Strength Index
     */
    private BigDecimal calculateRSI(List<SharePrice> prices, int currentIndex, int period) {
        if (currentIndex < period || prices == null || prices.isEmpty()) {
            return BigDecimal.valueOf(50); // Neutral RSI
        }
        
        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;
        
        for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
            if (i == 0) continue; // Skip first element as we need previous price
            
            BigDecimal currentPrice = prices.get(i).getClosePrice();
            BigDecimal previousPrice = prices.get(i - 1).getClosePrice();
            
            if (currentPrice != null && previousPrice != null) {
                BigDecimal change = currentPrice.subtract(previousPrice);
                
                if (change.compareTo(BigDecimal.ZERO) > 0) {
                    gains = gains.add(change);
                } else {
                    losses = losses.add(change.abs());
                }
            }
        }
        
        if (losses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        
        BigDecimal avgGain = gains.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), 4, RoundingMode.HALF_UP));
        
        return rsi;
    }
    
    /**
     * Get performance summary for a symbol
     */
    public PerformanceSummary getPerformanceSummary(String symbol, LocalDate startDate, LocalDate endDate) {
        validateInputs(symbol, startDate, endDate);
        
        try {
            List<SharePrice> prices = sharePriceService.getSharePrices(
                new ShareQuery(symbol, startDate, endDate));
            
            if (prices.isEmpty()) {
                throw new ServiceException("No data available for performance summary of " + symbol);
            }
            
            prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
            
            PerformanceSummary summary = new PerformanceSummary();
            summary.setSymbol(symbol);
            summary.setStartDate(startDate);
            summary.setEndDate(endDate);
            
            SharePrice firstPrice = prices.get(0);
            SharePrice lastPrice = prices.get(prices.size() - 1);
            
            summary.setStartPrice(firstPrice.getClosePrice());
            summary.setEndPrice(lastPrice.getClosePrice());
            
            if (summary.getStartPrice() != null && summary.getEndPrice() != null) {
                summary.setTotalReturn(summary.getEndPrice().subtract(summary.getStartPrice()));
                
                if (summary.getStartPrice().compareTo(BigDecimal.ZERO) != 0) {
                    summary.setPercentageReturn(summary.getTotalReturn()
                            .divide(summary.getStartPrice(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)));
                }
            }
            
            // Find high and low
            summary.setHighPrice(prices.stream()
                    .map(SharePrice::getHighPrice)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO));
                    
            summary.setLowPrice(prices.stream()
                    .map(SharePrice::getLowPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO));
            
            return summary;
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Error calculating performance summary for " + symbol, e);
        }
    }
    
    private void validateInputs(String symbol, LocalDate startDate, LocalDate endDate) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
    
    /**
     * Performance summary data class
     */
    public static class PerformanceSummary {
        private String symbol;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigDecimal startPrice;
        private BigDecimal endPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private BigDecimal totalReturn;
        private BigDecimal percentageReturn;
        
        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public BigDecimal getStartPrice() { return startPrice; }
        public void setStartPrice(BigDecimal startPrice) { this.startPrice = startPrice; }
        
        public BigDecimal getEndPrice() { return endPrice; }
        public void setEndPrice(BigDecimal endPrice) { this.endPrice = endPrice; }
        
        public BigDecimal getHighPrice() { return highPrice; }
        public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
        
        public BigDecimal getLowPrice() { return lowPrice; }
        public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
        
        public BigDecimal getTotalReturn() { return totalReturn; }
        public void setTotalReturn(BigDecimal totalReturn) { this.totalReturn = totalReturn; }
        
        public BigDecimal getPercentageReturn() { return percentageReturn; }
        public void setPercentageReturn(BigDecimal percentageReturn) { this.percentageReturn = percentageReturn; }
    }

    /**
     * Satisfies IPerformance.getSummary — delegates to getPerformanceSummary and
     * returns a plain Map so callers stay decoupled from the inner class.
     */
    @Override
    public java.util.Map<String, Object> getSummary(String symbol,
                                                     java.time.LocalDate startDate,
                                                     java.time.LocalDate endDate) {
        PerformanceSummary ps = getPerformanceSummary(symbol, startDate, endDate);
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("symbol",           ps.getSymbol());
        map.put("startDate",        ps.getStartDate());
        map.put("endDate",          ps.getEndDate());
        map.put("startPrice",       ps.getStartPrice());
        map.put("endPrice",         ps.getEndPrice());
        map.put("highPrice",        ps.getHighPrice());
        map.put("lowPrice",         ps.getLowPrice());
        map.put("totalReturn",      ps.getTotalReturn());
        map.put("percentageReturn", ps.getPercentageReturn());
        return map;
    }
}