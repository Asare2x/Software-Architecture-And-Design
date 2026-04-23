package com.example.trading.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PerformanceIndicator {
    private String symbol;
    private LocalDate date;
    private BigDecimal sma20; // 20-day Simple Moving Average
    private BigDecimal sma50; // 50-day Simple Moving Average
    private BigDecimal ema12; // 12-day Exponential Moving Average
    private BigDecimal ema26; // 26-day Exponential Moving Average
    private BigDecimal rsi;   // Relative Strength Index
    private BigDecimal macd;  // MACD Line
    private BigDecimal macdSignal; // MACD Signal Line
    
    public PerformanceIndicator() {}
    
    public PerformanceIndicator(String symbol, LocalDate date) {
        this.symbol = symbol;
        this.date = date;
    }
    
    // Getters and setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public BigDecimal getSma20() {
        return sma20;
    }
    
    public void setSma20(BigDecimal sma20) {
        this.sma20 = sma20;
    }
    
    public BigDecimal getSma50() {
        return sma50;
    }
    
    public void setSma50(BigDecimal sma50) {
        this.sma50 = sma50;
    }
    
    public BigDecimal getEma12() {
        return ema12;
    }
    
    public void setEma12(BigDecimal ema12) {
        this.ema12 = ema12;
    }
    
    public BigDecimal getEma26() {
        return ema26;
    }
    
    public void setEma26(BigDecimal ema26) {
        this.ema26 = ema26;
    }
    
    public BigDecimal getRsi() {
        return rsi;
    }
    
    public void setRsi(BigDecimal rsi) {
        this.rsi = rsi;
    }
    
    public BigDecimal getMacd() {
        return macd;
    }
    
    public void setMacd(BigDecimal macd) {
        this.macd = macd;
    }
    
    public BigDecimal getMacdSignal() {
        return macdSignal;
    }
    
    public void setMacdSignal(BigDecimal macdSignal) {
        this.macdSignal = macdSignal;
    }
    
    @Override
    public String toString() {
        return "PerformanceIndicator{" +
                "symbol='" + symbol + '\'' +
                ", date=" + date +
                ", sma20=" + sma20 +
                ", sma50=" + sma50 +
                ", rsi=" + rsi +
                ", macd=" + macd +
                '}';
    }
}
