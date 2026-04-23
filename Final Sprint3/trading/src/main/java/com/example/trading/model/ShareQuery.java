package com.example.trading.model;

import java.time.LocalDate;

public class ShareQuery {
    private String symbol;
    private LocalDate startDate;
    private LocalDate endDate;
    
    public ShareQuery() {}
    
    public ShareQuery(String symbol, LocalDate startDate, LocalDate endDate) {
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and setters
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isValid() {
        return symbol != null && !symbol.trim().isEmpty() 
               && startDate != null && endDate != null 
               && !startDate.isAfter(endDate);
    }
    
    @Override
    public String toString() {
        return "ShareQuery{" +
                "symbol='" + symbol + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
