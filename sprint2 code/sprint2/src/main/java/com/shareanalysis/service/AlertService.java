package com.shareanalysis.service;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.Alert;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Component: Alert Service
 * Direction: Alert Service ← SharePriceService (needs live data)
 *
 * Manages user-configured price alerts.
 * Evaluates active alerts against the latest price data from SharePriceService.
 */
public class AlertService {

    private final SharePriceService          sharePriceService;
    private final Map<String, Alert>         alerts = new LinkedHashMap<>();

    public AlertService(SharePriceService sharePriceService) {
        this.sharePriceService = sharePriceService;
    }

    /** Create and store a new price alert. */
    public Alert createAlert(String symbol, double threshold, Alert.Condition condition) {
        String alertId = UUID.randomUUID().toString().substring(0, 8);
        Alert alert = new Alert(alertId, symbol.toUpperCase().trim(), threshold, condition);
        alerts.put(alertId, alert);
        System.out.println("[AlertService] Alert created: " + alert);
        return alert;
    }

    /**
     * Evaluate all active alerts against the latest close price for each symbol.
     * Returns the list of alerts that were triggered.
     */
    public List<Alert> evaluateAlerts(ShareQuery query) throws ServiceException {
        List<SharePrice> prices = sharePriceService.getPrices(query);
        if (prices.isEmpty()) return Collections.emptyList();

        double latestClose = prices.get(prices.size() - 1).getClose();
        List<Alert> triggered = new ArrayList<>();

        alerts.values().stream()
                .filter(a -> a.getSymbol().equalsIgnoreCase(query.getSymbol()))
                .filter(a -> a.isTriggeredBy(latestClose))
                .forEach(a -> {
                    a.markTriggered();
                    triggered.add(a);
                    System.out.println("[AlertService] TRIGGERED: " + a
                            + " | Latest close: " + latestClose);
                });

        return triggered;
    }

    public List<Alert> getActiveAlerts() {
        return alerts.values().stream()
                .filter(Alert::isActive)
                .collect(Collectors.toList());
    }

    public void deleteAlert(String alertId) {
        alerts.remove(alertId);
        System.out.println("[AlertService] Deleted alert: " + alertId);
    }
}
