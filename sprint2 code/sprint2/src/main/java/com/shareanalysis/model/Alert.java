package com.shareanalysis.model;

import java.time.LocalDateTime;

/**
 * Represents a user-configured price alert.
 * Component: Alert Service — needs live data from SharePriceService.
 */
public class Alert {

    public enum Condition { ABOVE, BELOW }

    private final String    alertId;
    private final String    symbol;
    private final double    threshold;
    private final Condition condition;
    private       boolean       active;
    private       LocalDateTime triggeredAt;

    public Alert(String alertId, String symbol, double threshold, Condition condition) {
        this.alertId   = alertId;
        this.symbol    = symbol;
        this.threshold = threshold;
        this.condition = condition;
        this.active    = true;
    }

    public boolean isTriggeredBy(double price) {
        if (!active) return false;
        return condition == Condition.ABOVE ? price > threshold : price < threshold;
    }

    public void markTriggered() {
        this.triggeredAt = LocalDateTime.now();
        this.active      = false;
    }

    public String        getAlertId()     { return alertId;     }
    public String        getSymbol()      { return symbol;      }
    public double        getThreshold()   { return threshold;   }
    public Condition     getCondition()   { return condition;   }
    public boolean       isActive()       { return active;      }
    public LocalDateTime getTriggeredAt() { return triggeredAt; }

    @Override
    public String toString() {
        return String.format("Alert{id='%s', symbol='%s', %s %.2f, active=%s}",
                alertId, symbol, condition, threshold, active);
    }
}
