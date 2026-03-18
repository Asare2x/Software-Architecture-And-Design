package com.shareanalysis.model;

import java.util.List;

/**
 * Holds analytical performance indicators for a share.
 * Component: Performance Service — provides analytics data to Charting Service.
 */
public class PerformanceIndicator {

    private final String       symbol;
    private final List<Double> sma20;       // 20-day Simple Moving Average
    private final double       percentChange;
    private final double       high52Week;
    private final double       low52Week;

    public PerformanceIndicator(String symbol, List<Double> sma20,
                                double percentChange, double high52Week, double low52Week) {
        this.symbol        = symbol;
        this.sma20         = sma20;
        this.percentChange = percentChange;
        this.high52Week    = high52Week;
        this.low52Week     = low52Week;
    }

    public String       getSymbol()        { return symbol;        }
    public List<Double> getSma20()         { return sma20;         }
    public double       getPercentChange() { return percentChange; }
    public double       getHigh52Week()    { return high52Week;    }
    public double       getLow52Week()     { return low52Week;     }

    @Override
    public String toString() {
        return String.format("PerformanceIndicator{symbol='%s', change=%.2f%%, 52w-high=%.2f, 52w-low=%.2f}",
                symbol, percentChange, high52Week, low52Week);
    }
}
