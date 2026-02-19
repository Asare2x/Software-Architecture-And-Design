package com.example.demo1.model;

import java.time.LocalDate;

/**
 * Represents a single daily share price record for a given company.
 */
public class SharePrice {

    private final String symbol;       // Stock ticker symbol (e.g. "AAPL")
    private final LocalDate date;      // Trading date
    private final double openPrice;    // Opening price
    private final double closePrice;   // Closing price
    private final double highPrice;    // Daily high
    private final double lowPrice;     // Daily low
    private final long volume;         // Trade volume

    public SharePrice(String symbol, LocalDate date, double openPrice,
                      double closePrice, double highPrice, double lowPrice, long volume) {
        this.symbol = symbol;
        this.date = date;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.volume = volume;
    }

    // --- Getters ---
    public String getSymbol()      { return symbol; }
    public LocalDate getDate()     { return date; }
    public double getOpenPrice()   { return openPrice; }
    public double getClosePrice()  { return closePrice; }
    public double getHighPrice()   { return highPrice; }
    public double getLowPrice()    { return lowPrice; }
    public long getVolume()        { return volume; }

    @Override
    public String toString() {
        return String.format("SharePrice[symbol=%s, date=%s, close=%.2f]",
                symbol, date, closePrice);
    }
}
