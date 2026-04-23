package com.example.trading;

import com.example.trading.adapter.MarketDataAdapter;
import com.example.trading.api.IAPI;
import com.example.trading.blackboard.MarketDataBlackboard;
import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Blackboard and Adapter architectural patterns.
 *
 * Tests cover:
 *  TC-BB-01   Post and retrieve latest price from blackboard
 *  TC-BB-02   Blackboard observer notified on new price
 *  TC-BB-03   Post batch updates history correctly
 *  TC-BB-04   Blackboard tracks multiple symbols independently
 *  TC-ADP-01  Adapter wraps IAPI correctly
 *  TC-ADP-02  Adapter propagates DataProviderException
 *  TC-ADP-03  Adapter returns empty list when provider unavailable
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BlackboardAndAdapterTest {

    private MarketDataBlackboard blackboard;

    @BeforeEach
    void setUp() {
        blackboard = MarketDataBlackboard.getInstance();
        blackboard.clear();
    }

    // TC-BB-01
    @Test @Order(1)
    @DisplayName("TC-BB-01: Post and retrieve latest price")
    void testPostAndRetrieve() {
        SharePrice price = makePrice("AAPL", 175.0);
        blackboard.post("AAPL", price);
        Optional<SharePrice> retrieved = blackboard.getLatest("AAPL");
        assertTrue(retrieved.isPresent());
        assertEquals(175.0, retrieved.get().getClosePriceAsDouble(), 0.01);
    }

    // TC-BB-02
    @Test @Order(2)
    @DisplayName("TC-BB-02: Observer is notified when price is posted")
    void testObserverNotified() {
        AtomicBoolean notified = new AtomicBoolean(false);
        blackboard.addObserver((sym, p) -> notified.set(true));
        blackboard.post("MSFT", makePrice("MSFT", 415.0));
        assertTrue(notified.get(), "Observer should have been notified");
    }

    // TC-BB-03
    @Test @Order(3)
    @DisplayName("TC-BB-03: Batch post builds history correctly")
    void testBatchPost() {
        List<SharePrice> batch = List.of(
                makePrice("TSLA", 240.0),
                makePrice("TSLA", 245.0),
                makePrice("TSLA", 250.0));
        blackboard.postBatch("TSLA", batch);
        List<SharePrice> history = blackboard.getHistory("TSLA");
        assertEquals(3, history.size());
    }

    // TC-BB-04
    @Test @Order(4)
    @DisplayName("TC-BB-04: Multiple symbols tracked independently")
    void testMultipleSymbols() {
        blackboard.post("AAPL", makePrice("AAPL", 175.0));
        blackboard.post("NVDA", makePrice("NVDA", 875.0));
        assertEquals(175.0, blackboard.getLatest("AAPL").get().getClosePriceAsDouble(), 0.01);
        assertEquals(875.0, blackboard.getLatest("NVDA").get().getClosePriceAsDouble(), 0.01);
        assertTrue(blackboard.getTrackedSymbols().containsAll(List.of("AAPL", "NVDA")));
    }

    // TC-ADP-01
    @Test @Order(5)
    @DisplayName("TC-ADP-01: Adapter delegates to wrapped IAPI")
    void testAdapterDelegates() throws Exception {
        MarketDataAdapter adapter = new MarketDataAdapter(new StubAPI(false));
        List<SharePrice> prices = adapter.getSharePrices(
                new ShareQuery("AAPL", LocalDate.now().minusDays(5), LocalDate.now()));
        assertFalse(prices.isEmpty(), "Adapter should return prices from wrapped API");
    }

    // TC-ADP-02
    @Test @Order(6)
    @DisplayName("TC-ADP-02: Adapter wraps API exception as DataProviderException")
    void testAdapterWrapsException() {
        MarketDataAdapter adapter = new MarketDataAdapter(new StubAPI(true));
        assertThrows(DataProviderException.class,
                () -> adapter.getSharePrices(
                        new ShareQuery("AAPL", LocalDate.now().minusDays(5), LocalDate.now())));
    }

    // TC-ADP-03
    @Test @Order(7)
    @DisplayName("TC-ADP-03: Adapter reports unavailable when API is down")
    void testAdapterUnavailable() {
        MarketDataAdapter adapter = new MarketDataAdapter(new StubAPI(true));
        assertFalse(adapter.isAvailable());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SharePrice makePrice(String symbol, double close) {
        return new SharePrice(symbol, LocalDate.now(),
                BigDecimal.valueOf(close - 1), BigDecimal.valueOf(close),
                BigDecimal.valueOf(close + 2), BigDecimal.valueOf(close - 2),
                1_000_000L);
    }

    static class StubAPI implements IAPI {
        private final boolean fail;
        StubAPI(boolean fail) { this.fail = fail; }

        @Override
        public List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException {
            if (fail) throw new DataProviderException("Stub API failure");
            return List.of(new SharePrice(query.getSymbol(), LocalDate.now(),
                    BigDecimal.valueOf(149), BigDecimal.valueOf(150),
                    BigDecimal.valueOf(152), BigDecimal.valueOf(148), 500_000L));
        }

        @Override public boolean isAvailable()     { return !fail; }
        @Override public String  getProviderName() { return "StubAPI"; }
    }
}
// Note: PipelineFilter tests are in PipelineFilterTest.java
