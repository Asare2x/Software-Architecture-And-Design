package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.Account;
import com.example.trading.model.Alert;
import com.example.trading.model.SharePrice;
import com.example.trading.repository.IAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AlertService implements IAlert {
    
    private final AccountWalletService accountService;
    private final IPriceService sharePriceService;
    private final IAccountRepository accountRepository;
    
    public AlertService(AccountWalletService accountService, IPriceService sharePriceService, IAccountRepository accountRepository) {
        this.accountService = accountService;
        this.sharePriceService = sharePriceService;
        this.accountRepository = accountRepository;
    }
    
    /**
     * Create a new price alert
     */
    public Alert createAlert(String symbol, BigDecimal targetPrice, Alert.AlertType alertType) {
        // Add validation
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new ServiceException("Symbol cannot be null or empty");
        }
        if (targetPrice == null) {
            throw new ServiceException("Target price cannot be null");
        }
        if (alertType == null) {
            throw new ServiceException("Alert type cannot be null");
        }
        if (targetPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Target price must be positive");
        }
        
        // Validate symbol format (basic validation)
        String normalizedSymbol = symbol.toUpperCase().trim();
        if (!normalizedSymbol.matches("^[A-Z]{1,5}$")) {
            throw new ServiceException("Invalid symbol format");
        }
        
        try {
            Account account = accountService.getCurrentUserAccount();
        
            // Check for duplicate alerts
            boolean duplicateExists = account.getAlerts().stream()
                .anyMatch(a -> a.isActive() && 
                         a.getSymbol().equals(normalizedSymbol) && 
                         a.getTargetPrice().compareTo(targetPrice) == 0 &&
                         a.getAlertType() == alertType);
        
            if (duplicateExists) {
                throw new ServiceException("Alert already exists for this symbol and price");
            }
        
            Alert alert = new Alert(normalizedSymbol, targetPrice, alertType);
            alert.setAlertId(UUID.randomUUID().toString());
        
            account.addAlert(alert);
            // Persist account changes
            accountRepository.save(account);
            
            System.out.println("Created alert: " + alert.getAlertId() + " for symbol: " + normalizedSymbol);
        
            return alert;
        } catch (ServiceException e) {
            throw e; // Re-throw service exceptions
        } catch (Exception e) {
            System.err.println("Error creating alert: " + e.getMessage());
            throw new ServiceException("Error creating alert", e);
        }
    }

    /**
     * Get all active alerts for current user
     */
    public List<Alert> getActiveAlerts() {
        Account account = accountService.getCurrentUserAccount();
        return account.getAlerts().stream()
                .filter(Alert::isActive)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all alerts for current user
     */
    public List<Alert> getAllAlerts() {
        Account account = accountService.getCurrentUserAccount();
        return account.getAlerts();
    }
    
    /**
     * Cancel an alert
     */
    public void cancelAlert(String alertId) {
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new ServiceException("Alert ID cannot be null or empty");
        }

        try {
            Account account = accountService.getCurrentUserAccount();
            Alert alert = account.getAlerts().stream()
                    .filter(a -> a.getAlertId() != null && a.getAlertId().equals(alertId.trim()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("Alert not found"));

            alert.setActive(false);
            // Persist account changes
            accountRepository.save(account);
            
            System.out.println("Cancelled alert: " + alertId);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error cancelling alert: " + e.getMessage());
            throw new ServiceException("Error cancelling alert", e);
        }
    }
    
    /**
     * Check all active alerts against current prices
     */
    public List<Alert> checkAlerts() {
        try {
            List<Alert> triggeredAlerts = getActiveAlerts().stream()
                    .filter(this::checkAlert)
                    .collect(Collectors.toList());
            
            // Trigger the alerts
            if (!triggeredAlerts.isEmpty()) {
                triggeredAlerts.forEach(Alert::trigger);
                
                // Persist changes
                Account account = accountService.getCurrentUserAccount();
                accountRepository.save(account);
                
                System.out.println("Triggered " + triggeredAlerts.size() + " alerts");
            }
            
            return triggeredAlerts;
        } catch (Exception e) {
            System.err.println("Error checking alerts: " + e.getMessage());
            throw new ServiceException("Error checking alerts", e);
        }
    }
    
    /**
     * Check if a single alert should be triggered
     */
    private boolean checkAlert(Alert alert) {
        try {
            SharePrice currentPrice = sharePriceService.getLatestPrice(alert.getSymbol());
            if (currentPrice == null) {
                System.out.println("No current price available for symbol: " + alert.getSymbol());
                return false;
            }
            
            BigDecimal price = currentPrice.getClosePrice();
            if (price == null) {
                System.out.println("Close price is null for symbol: " + alert.getSymbol());
                return false;
            }
            
            BigDecimal target = alert.getTargetPrice();
            
            switch (alert.getAlertType()) {
                case PRICE_ABOVE:
                    return price.compareTo(target) >= 0;
                case PRICE_BELOW:
                    return price.compareTo(target) <= 0;
                case PERCENT_CHANGE_UP:
                case PERCENT_CHANGE_DOWN:
                    // For percentage changes, you'd need to compare with previous day
                    // Simplified implementation - returns false for now
                    return false;
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Error checking alert for symbol: " + alert.getSymbol() + " - " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get alerts for a specific symbol
     */
    public List<Alert> getAlertsForSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new ServiceException("Symbol cannot be null or empty");
        }
        
        String normalizedSymbol = symbol.toUpperCase().trim();
        return getAllAlerts().stream()
                .filter(alert -> alert.getSymbol().equals(normalizedSymbol))
                .collect(Collectors.toList());
    }
    
    /**
     * Delete an alert permanently
     */
    public void deleteAlert(String alertId) {
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new ServiceException("Alert ID cannot be null or empty");
        }

        try {
            Account account = accountService.getCurrentUserAccount();
            Alert alert = account.getAlerts().stream()
                    .filter(a -> a.getAlertId() != null && a.getAlertId().equals(alertId.trim()))
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("Alert not found"));

            account.removeAlert(alert);
            // Persist account changes
            accountRepository.save(account);
            
            System.out.println("Deleted alert: " + alertId);
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("Error deleting alert: " + e.getMessage());
            throw new ServiceException("Error deleting alert", e);
        }
    }
    
    /**
     * Get alert statistics for current user
     */
    public AlertStatistics getAlertStatistics() {
        try {
            Account account = accountService.getCurrentUserAccount();
            List<Alert> allAlerts = account.getAlerts();
            
            long activeCount = allAlerts.stream().filter(Alert::isActive).count();
            long triggeredCount = allAlerts.stream()
                    .filter(alert -> !alert.isActive() && alert.getTriggeredAt() != null)
                    .count();
            long cancelledCount = allAlerts.stream()
                    .filter(alert -> !alert.isActive() && alert.getTriggeredAt() == null)
                    .count();
            
            return new AlertStatistics(activeCount, triggeredCount, cancelledCount);
            
        } catch (Exception e) {
            System.err.println("Error getting alert statistics: " + e.getMessage());
            throw new ServiceException("Error getting alert statistics", e);
        }
    }
    
    /**
     * Clean up old triggered alerts (older than specified days)
     */
    public int cleanupOldAlerts(int daysOld) {
        try {
            Account account = accountService.getCurrentUserAccount();
            java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(daysOld);
            
            List<Alert> alertsToRemove = account.getAlerts().stream()
                    .filter(alert -> !alert.isActive() && 
                            alert.getTriggeredAt() != null &&
                            alert.getTriggeredAt().isBefore(cutoffDate))
                    .collect(Collectors.toList());
            
            alertsToRemove.forEach(account::removeAlert);
            
            if (!alertsToRemove.isEmpty()) {
                accountRepository.save(account);
                System.out.println("Cleaned up " + alertsToRemove.size() + " old alerts");
            }
            
            return alertsToRemove.size();
            
        } catch (Exception e) {
            System.err.println("Error cleaning up old alerts: " + e.getMessage());
            throw new ServiceException("Error cleaning up old alerts", e);
        }
    }
    
    /**
     * Inner class for alert statistics
     */
    public static class AlertStatistics {
        private final long activeAlerts;
        private final long triggeredAlerts;
        private final long cancelledAlerts;
        
        public AlertStatistics(long activeAlerts, long triggeredAlerts, long cancelledAlerts) {
            this.activeAlerts = activeAlerts;
            this.triggeredAlerts = triggeredAlerts;
            this.cancelledAlerts = cancelledAlerts;
        }
        
        public long getActiveAlerts() { return activeAlerts; }
        public long getTriggeredAlerts() { return triggeredAlerts; }
        public long getCancelledAlerts() { return cancelledAlerts; }
        public long getTotalAlerts() { return activeAlerts + triggeredAlerts + cancelledAlerts; }
        
        @Override
        public String toString() {
            return "AlertStatistics{" +
                    "activeAlerts=" + activeAlerts +
                    ", triggeredAlerts=" + triggeredAlerts +
                    ", cancelledAlerts=" + cancelledAlerts +
                    ", totalAlerts=" + getTotalAlerts() +
                    '}';
        }
    }
}
