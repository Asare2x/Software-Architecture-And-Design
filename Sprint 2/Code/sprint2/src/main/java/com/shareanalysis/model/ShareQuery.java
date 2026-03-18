package com.shareanalysis.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Encapsulates a user's request for share price data.
 * Enforces the 2-year maximum date range business rule.
 */
public class ShareQuery {

    private static final long MAX_RANGE_DAYS = 730;

    private final String    symbol;
    private final LocalDate from;
    private final LocalDate to;

    public ShareQuery(String symbol, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        long days = ChronoUnit.DAYS.between(from, to);
        if (days > MAX_RANGE_DAYS) {
            throw new IllegalArgumentException(
                "Date range must not exceed two years. Requested: " + days + " days.");
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
