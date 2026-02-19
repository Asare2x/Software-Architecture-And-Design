package com.example.demo1.model;

/**
 * Represents a publicly listed company with a stock ticker symbol.
 */
public class Company {

    private final String symbol;   // e.g. "AAPL"
    private final String name;     // e.g. "Apple Inc."
    private final String exchange; // e.g. "NASDAQ"

    public Company(String symbol, String name, String exchange) {
        this.symbol = symbol;
        this.name = name;
        this.exchange = exchange;
    }

    public String getSymbol()   { return symbol; }
    public String getName()     { return name; }
    public String getExchange() { return exchange; }

    @Override
    public String toString() {
        return String.format("Company[symbol=%s, name=%s, exchange=%s]",
                symbol, name, exchange);
    }
}
