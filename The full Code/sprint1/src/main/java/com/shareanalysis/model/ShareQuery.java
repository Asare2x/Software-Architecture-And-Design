package com.shareanalysis.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value object that encapsulates a user's data query.
 *
 * Enforces the business rule that the date range cannot exceed two years.
 */
public class ShareQuery {

    /** Maximum allowed date range in days (approx. 2 years). */
    private static final long MAX_RANGE_DAYS = 730;

    private final String    symbol;
    private final LocalDate from;
    private final LocalDate to;

    /**
     * @param symbol Ticker symbol (e.g. "AAPL")
     * @param from   Start date (inclusive)
     * @param to     End date   (inclusive)
     * @throws IllegalArgumentException if the range exceeds two years
     */
    public ShareQuery(String symbol, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' date must be before 'to' date.");
        }
        long days = ChronoUnit.DAYS.between(from, to);
        if (days > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException(
                    "Date range must not exceed two years (730 days). Requested: " + days + " days.");
        }
        this.symbol = symbol.toUpperCase().trim();
        this.from   = from;
        this.to     = to;
    }

    public String    getSymbol() { return symbol; }
    public LocalDate getFrom()   { return from;   }
    public LocalDate getTo()     { return to;     }

    @Override
    public String toString() {
        return String.format("ShareQuery{symbol='%s', from=%s, to=%s}", symbol, from, to);
    }
}
