package com.example.trading.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private String accountId;
    private String username;
    private BigDecimal balance;
    private List<String> watchlist;
    private List<Alert> alerts;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    
    public Account() {
        this.watchlist = new ArrayList<>();
        this.alerts = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.balance = BigDecimal.ZERO;
    }
    
    public Account(String accountId, String username) {
        this();
        this.accountId = accountId;
        this.username = username;
    }
    
    // Getters and Setters
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public List<String> getWatchlist() { return new ArrayList<>(watchlist); }
    public void setWatchlist(List<String> watchlist) { this.watchlist = new ArrayList<>(watchlist); }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    
    // Business methods
    public void addToWatchlist(String symbol) {
        if (!watchlist.contains(symbol)) {
            watchlist.add(symbol);
        }
    }
    
    public void removeFromWatchlist(String symbol) {
        watchlist.remove(symbol);
    }
    
    public boolean isInWatchlist(String symbol) {
        return watchlist.contains(symbol);
    }
    
    public List<Alert> getAlerts() { 
        return new ArrayList<>(alerts); 
    }
    
    public void setAlerts(List<Alert> alerts) { 
        this.alerts = new ArrayList<>(alerts); 
    }
    
    public void addAlert(Alert alert) {
        if (alert != null) {
            alerts.add(alert);
        }
    }
    
    public void removeAlert(Alert alert) {
        alerts.remove(alert);
    }
}
