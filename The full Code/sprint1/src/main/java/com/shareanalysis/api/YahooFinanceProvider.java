package com.shareanalysis.api;

import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Concrete implementation of {@link ShareDataProvider} targeting Yahoo Finance.
 *
 * Sprint 1 Note: This is an ABSTRACT/STUB implementation that generates
 * synthetic data so that the architectural skeleton compiles and runs.
 * A real HTTP client (e.g. OkHttp + Yahoo Finance API) will replace
 * the stub body in Sprint 2.
 *
 * Architecture role: External API Adapter
 *  - Translates Yahoo Finance responses into domain {@link SharePrice} objects.
 *  - Isolates the rest of the application from third-party data formats.
 */
public class YahooFinanceProvider implements ShareDataProvider {

    // Base URL for the real Yahoo Finance v8 endpoint (wired in Sprint 2)
    private static final String YAHOO_BASE_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/";

    /**
     * Fetches daily prices.
     *
     * Sprint 1: Returns deterministic synthetic data for demonstration.
     * Sprint 2: Will perform a real HTTP GET to the Yahoo Finance API.
     */
    @Override
    public List<SharePrice> fetchPrices(ShareQuery query) throws DataProviderException {
        System.out.println("[YahooFinanceProvider] Fetching prices for: " + query);

        // --- STUB: generate synthetic OHLCV data ---
        List<SharePrice> prices = new ArrayList<>();
        LocalDate current = query.getFrom();
        Random rng = new Random(query.getSymbol().hashCode()); // repeatable per symbol
        double price = 100.0 + rng.nextDouble() * 400.0;       // start price $100–$500

        while (!current.isAfter(query.getTo())) {
            // Skip weekends (markets closed)
            int dayOfWeek = current.getDayOfWeek().getValue(); // 1=Mon … 7=Sun
            if (dayOfWeek <= 5) {
                double change = (rng.nextDouble() - 0.48) * 5; // slight upward bias
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

        System.out.println("[YahooFinanceProvider] Generated " + prices.size() + " price records (stub).");
        return prices;
    }

    /**
     * Sprint 1: Stub — always reports available.
     * Sprint 2: Will perform an actual connectivity check.
     */
    @Override
    public boolean isAvailable() {
        return true;
    }
}
