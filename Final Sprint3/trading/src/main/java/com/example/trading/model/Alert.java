package com.example.trading.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Alert {
    private String alertId;
    private String symbol;
    private BigDecimal targetPrice;
    private AlertType alertType;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime triggeredAt;
    
    public enum AlertType {
        PRICE_ABOVE,
        PRICE_BELOW,
        PERCENT_CHANGE_UP,
        PERCENT_CHANGE_DOWN
    }
    
    public Alert() {
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }
    
    public Alert(String symbol, BigDecimal targetPrice, AlertType alertType) {
        this();
        this.symbol = symbol;
        this.targetPrice = targetPrice;
        this.alertType = alertType;
    }
    
    // Getters and setters
    public String getAlertId() {
        return alertId;
    }
    
    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public BigDecimal getTargetPrice() {
        return targetPrice;
    }
    
    public void setTargetPrice(BigDecimal targetPrice) {
        this.targetPrice = targetPrice;
    }
    
    public AlertType getAlertType() {
        return alertType;
    }
    
    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }
    
    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }
    
    public void trigger() {
        this.isActive = false;
        this.triggeredAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Alert{" +
                "alertId='" + alertId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", targetPrice=" + targetPrice +
                ", alertType=" + alertType +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
