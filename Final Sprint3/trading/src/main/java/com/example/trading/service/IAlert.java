package com.example.trading.service;

import com.example.trading.model.Alert;
import java.math.BigDecimal;
import java.util.List;

/**
 * Alert service contract — implemented by AlertService.
 */
public interface IAlert {
    Alert        createAlert(String symbol, BigDecimal targetPrice, Alert.AlertType alertType);
    List<Alert>  getActiveAlerts();
    List<Alert>  getAllAlerts();
    void         cancelAlert(String alertId);
    void         deleteAlert(String alertId);
    List<Alert>  checkAlerts();
    List<Alert>  getAlertsForSymbol(String symbol);
}
