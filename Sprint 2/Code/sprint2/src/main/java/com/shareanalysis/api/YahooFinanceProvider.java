package com.shareanalysis.api;

import com.shareanalysis.exception.DataProviderException;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Component: Data Market (external data feed) implemented via Yahoo Finance.
 * Direction: Data Market → SharePriceRepository → SharePriceService
 *
 * Sprint 2: Stub implementation generates realistic synthetic OHLCV data.
 * Sprint 3: Replace stub body with a real HTTP GET to:
 *   https://query1.finance.yahoo.com/v8/finance/chart/{symbol}
 *
 * No other class needs to change when the stub is replaced —
 * everything depends on the ShareDataProvider interface only.
 */
public class YahooFinanceProvider implements ShareDataProvider {

    @Override
    public List<SharePrice> fetchPrices(ShareQuery query) throws DataProviderException {
        System.out.println("[DataMarket/YahooFinance] Fetching: " + query);

        List<SharePrice> prices = new ArrayList<>();
        LocalDate current = query.getFrom();
        Random rng   = new Random(query.getSymbol().hashCode());
        double price = 100.0 + rng.nextDouble() * 400.0;

        while (!current.isAfter(query.getTo())) {
            int dow = current.getDayOfWeek().getValue();
            if (dow <= 5) { // skip weekends
                double change = (rng.nextDouble() - 0.48) * 5;
                double open   = price;
                double close  = Math.max(1.0, price + change);
                double high   = Math.max(open, close) + rng.nextDouble() * 2;
                double low    = Math.min(open, close) - rng.nextDouble() * 2;
                long   volume = 1_000_000L + (long)(rng.nextDouble() * 9_000_000);

                prices.add(new SharePrice(query.getSymbol(), current,
                        open, high, low, close, volume));
                price = close;
            }
            current = current.plusDays(1);
        }

        System.out.println("[DataMarket/YahooFinance] Generated " + prices.size() + " records (stub).");
        return prices;
    }

    @Override
    public boolean isAvailable() {
        return true; // Sprint 3: real connectivity check
    }
}
