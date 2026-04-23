package com.example.trading.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class SharePrice {
    private String symbol;
    private LocalDate date;
    private LocalDateTime timestamp;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private long volume;
    
    public SharePrice() {
    }
    
    public SharePrice(String symbol, LocalDate date, BigDecimal openPrice, 
                     BigDecimal closePrice, BigDecimal highPrice, BigDecimal lowPrice, long volume) {
        this.symbol = symbol;
        this.date = date;
        this.timestamp = date != null ? date.atStartOfDay() : null;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
    }
    
    // Backward compatibility constructor with double values
    public SharePrice(String symbol, LocalDate date, double openPrice, 
                     double closePrice, double highPrice, double lowPrice, long volume) {
        this(symbol, date, 
             BigDecimal.valueOf(openPrice),
             BigDecimal.valueOf(closePrice),
             BigDecimal.valueOf(highPrice),
             BigDecimal.valueOf(lowPrice),
             volume);
    }
    
    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { 
        this.symbol = symbol != null ? symbol.toUpperCase() : null; 
    }
    
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { 
        this.date = date; 
        this.timestamp = date != null ? date.atStartOfDay() : null;
    }
    
    public LocalDateTime getTimestamp() { 
        return timestamp != null ? timestamp : (date != null ? date.atStartOfDay() : null); 
    }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
        this.date = timestamp != null ? timestamp.toLocalDate() : null;
    }
    
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    
    // Backward compatibility
    public double getOpenPriceAsDouble() { 
        return openPrice != null ? openPrice.doubleValue() : 0.0; 
    }
    public void setOpenPrice(double openPrice) { 
        this.openPrice = BigDecimal.valueOf(openPrice); 
    }
    
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    
    public double getClosePriceAsDouble() { 
        return closePrice != null ? closePrice.doubleValue() : 0.0; 
    }
    public void setClosePrice(double closePrice) { 
        this.closePrice = BigDecimal.valueOf(closePrice); 
    }
    
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    
    public double getHighPriceAsDouble() { 
        return highPrice != null ? highPrice.doubleValue() : 0.0; 
    }
    public void setHighPrice(double highPrice) { 
        this.highPrice = BigDecimal.valueOf(highPrice); 
    }
    
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    
    public double getLowPriceAsDouble() { 
        return lowPrice != null ? lowPrice.doubleValue() : 0.0; 
    }
    public void setLowPrice(double lowPrice) { 
        this.lowPrice = BigDecimal.valueOf(lowPrice); 
    }
    
    public long getVolume() { return volume; }
    public void setVolume(long volume) { this.volume = volume; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SharePrice that = (SharePrice) o;
        return Objects.equals(symbol, that.symbol) && Objects.equals(date, that.date);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(symbol, date);
    }
    
    @Override
    public String toString() {
        return String.format("SharePrice{symbol='%s', date=%s, close=%s}", 
                           symbol, date, closePrice);
    }
}
