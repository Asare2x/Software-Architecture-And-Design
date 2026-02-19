package com.shareanalysis.model;

import java.time.LocalDate;

/**
 * Domain model representing a single day's price data for a share.
 *
 * Encapsulates the OHLCV (Open, High, Low, Close, Volume) data
 * that is standard in financial time-series analysis.
 */
public class SharePrice {

    private final String    symbol;    // e.g. "AAPL", "TSLA"
    private final LocalDate date;
    private final double    open;
    private final double    high;
    private final double    low;
    private final double    close;     // adjusted closing price
    private final long      volume;

    public SharePrice(String symbol, LocalDate date,
                      double open, double high, double low,
                      double close, long volume) {
        this.symbol = symbol;
        this.date   = date;
        this.open   = open;
        this.high   = high;
        this.low    = low;
        this.close  = close;
        this.volume = volume;
    }

    // --- Getters ---

    public String    getSymbol() { return symbol; }
    public LocalDate getDate()   { return date;   }
    public double    getOpen()   { return open;   }
    public double    getHigh()   { return high;   }
    public double    getLow()    { return low;    }
    public double    getClose()  { return close;  }
    public long      getVolume() { return volume; }

    @Override
    public String toString() {
        return String.format("SharePrice{symbol='%s', date=%s, close=%.2f}",
                symbol, date, close);
    }
}
