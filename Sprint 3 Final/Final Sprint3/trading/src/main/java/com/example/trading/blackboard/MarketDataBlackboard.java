package com.example.trading.blackboard;

import com.example.trading.model.SharePrice;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BLACKBOARD PATTERN
 *
 * The Blackboard acts as a shared, central knowledge base that multiple
 * independent components ("knowledge sources") can read from and write to.
 *
 * In this trading system:
 *   - SharePriceService writes fetched prices to the blackboard
 *   - AlertService reads from it to check trigger conditions
 *   - ChartingService reads from it to render charts
 *   - PerformanceService reads from it for analysis
 *
 * This decouples services from each other — they communicate only
 * through the shared blackboard, not through direct method calls.
 *
 * Architecture role: Domain-Independent shared state store.
 */
public class MarketDataBlackboard {

    // ── Singleton ────────────────────────────────────────────────────────────
    private static MarketDataBlackboard instance;

    public static synchronized MarketDataBlackboard getInstance() {
        if (instance == null) instance = new MarketDataBlackboard();
        return instance;
    }

    // ── State ────────────────────────────────────────────────────────────────
    /** Latest known price per symbol */
    private final Map<String, SharePrice>        latestPrices = new ConcurrentHashMap<>();

    /** Price history per symbol (capped to avoid unbounded growth) */
    private final Map<String, List<SharePrice>>  priceHistory = new ConcurrentHashMap<>();

    /** Timestamp of last update per symbol */
    private final Map<String, LocalDateTime>     lastUpdated  = new ConcurrentHashMap<>();

    /** Observers notified when new data arrives */
    private final List<BlackboardObserver>       observers    = new ArrayList<>();

    private static final int MAX_HISTORY = 500;

    private MarketDataBlackboard() {}

    // ── Write ────────────────────────────────────────────────────────────────

    /**
     * Post new price data to the blackboard.
     * All registered observers are notified.
     */
    public synchronized void post(String symbol, SharePrice price) {
        if (symbol == null || price == null) return;
        symbol = symbol.toUpperCase();
        latestPrices.put(symbol, price);
        lastUpdated.put(symbol, LocalDateTime.now());
        priceHistory.computeIfAbsent(symbol, k -> new ArrayList<>()).add(price);
        // Cap history size
        List<SharePrice> history = priceHistory.get(symbol);
        if (history.size() > MAX_HISTORY)
            history.remove(0);
        notifyObservers(symbol, price);
    }

    /**
     * Post a batch of prices — more efficient than posting one at a time.
     */
    public synchronized void postBatch(String symbol, List<SharePrice> prices) {
        if (prices == null || prices.isEmpty()) return;
        prices.forEach(p -> post(symbol, p));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    public Optional<SharePrice> getLatest(String symbol) {
        return Optional.ofNullable(latestPrices.get(symbol.toUpperCase()));
    }

    public List<SharePrice> getHistory(String symbol) {
        return new ArrayList<>(priceHistory.getOrDefault(symbol.toUpperCase(), Collections.emptyList()));
    }

    public Set<String> getTrackedSymbols() {
        return Collections.unmodifiableSet(latestPrices.keySet());
    }

    public Optional<LocalDateTime> getLastUpdated(String symbol) {
        return Optional.ofNullable(lastUpdated.get(symbol.toUpperCase()));
    }

    public boolean hasData(String symbol) {
        return latestPrices.containsKey(symbol.toUpperCase());
    }

    // ── Observer support ─────────────────────────────────────────────────────

    public void addObserver(BlackboardObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(BlackboardObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String symbol, SharePrice price) {
        for (BlackboardObserver o : observers) {
            try { o.onPriceUpdated(symbol, price); }
            catch (Exception ignored) {}
        }
    }

    /** Clear all data (useful for testing) */
    public synchronized void clear() {
        latestPrices.clear();
        priceHistory.clear();
        lastUpdated.clear();
    }

    // ── Observer interface ────────────────────────────────────────────────────

    /**
     * Knowledge sources implement this to be notified when the blackboard
     * receives new price data.
     */
    @FunctionalInterface
    public interface BlackboardObserver {
        void onPriceUpdated(String symbol, SharePrice latestPrice);
    }
}
