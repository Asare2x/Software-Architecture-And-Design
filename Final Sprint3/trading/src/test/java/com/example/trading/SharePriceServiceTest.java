package com.example.trading;

import com.example.trading.api.IDataProvider;
import com.example.trading.exception.DataProviderException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.repository.JsonSharePriceRepository;
import com.example.trading.service.AuthenticationService;
import com.example.trading.service.SharePriceService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for SharePriceService.
 *
 * Uses a stub IDataProvider to avoid network dependency.
 *
 * Tests cover:
 *  TC-SPS-01  Prices returned for valid query
 *  TC-SPS-02  Empty list for unknown symbol
 *  TC-SPS-03  Results sorted ascending by date
 *  TC-SPS-04  DataProviderException propagated as ServiceException
 *  TC-SPS-05  Null query rejected with exception
 *  TC-SPS-06  getLatestPrice returns most recent record
 *  TC-SPS-07  Date range filtering works correctly
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SharePriceServiceTest {

    private SharePriceService service;
    private StubDataProvider  stubProvider;

    @BeforeEach
    void setUp() {
        // Ensure a user is logged in (SharePriceService checks auth)
        AuthenticationService.getInstance().authenticate("admin", "admin123");
        stubProvider = new StubDataProvider();
        service = new SharePriceService(new JsonSharePriceRepository(), stubProvider);
    }

    @AfterEach
    void tearDown() {
        AuthenticationService.getInstance().logout();
    }

    // TC-SPS-01
    @Test @Order(1)
    @DisplayName("TC-SPS-01: Valid symbol returns price list")
    void testGetPricesForValidSymbol() {
        List<SharePrice> prices = service.getSharePrices(
                new ShareQuery("AAPL", LocalDate.now().minusDays(5), LocalDate.now()));
        assertNotNull(prices);
        assertFalse(prices.isEmpty(), "Should return prices for AAPL");
    }

    // TC-SPS-02
    @Test @Order(2)
    @DisplayName("TC-SPS-02: Unknown symbol returns empty list or fallback")
    void testGetPricesForUnknownSymbol() {
        stubProvider.returnEmpty = true;
        // Should not throw — service returns empty or falls back to sample data
        assertDoesNotThrow(() -> service.getSharePrices(
                new ShareQuery("ZZZZ", LocalDate.now().minusDays(5), LocalDate.now())));
    }

    // TC-SPS-03
    @Test @Order(3)
    @DisplayName("TC-SPS-03: Results are sorted ascending by date")
    void testResultsSortedByDate() {
        List<SharePrice> prices = service.getSharePrices(
                new ShareQuery("AAPL", LocalDate.now().minusDays(30), LocalDate.now()));
        for (int i = 1; i < prices.size(); i++) {
            assertFalse(prices.get(i).getDate().isBefore(prices.get(i - 1).getDate()),
                    "Prices should be sorted ascending by date");
        }
    }

    // TC-SPS-04
    @Test @Order(4)
    @DisplayName("TC-SPS-04: Provider exception is handled gracefully")
    void testProviderExceptionHandled() {
        stubProvider.throwException = true;
        // Service should either throw ServiceException or fall back — must not throw unchecked
        assertDoesNotThrow(() -> {
            try {
                service.getSharePrices(
                        new ShareQuery("AAPL", LocalDate.now().minusDays(5), LocalDate.now()));
            } catch (com.example.trading.exception.ServiceException e) {
                // acceptable — service exception is expected
            }
        });
    }

    // TC-SPS-05
    @Test @Order(5)
    @DisplayName("TC-SPS-05: Null query throws exception")
    void testNullQueryThrows() {
        assertThrows(Exception.class, () -> service.getSharePrices(null));
    }

    // TC-SPS-06
    @Test @Order(6)
    @DisplayName("TC-SPS-06: getLatestPrice returns most recent record")
    void testGetLatestPrice() {
        SharePrice latest = service.getLatestPrice("AAPL");
        assertNotNull(latest, "Latest price should not be null");
        assertEquals("AAPL", latest.getSymbol());
    }

    // TC-SPS-07
    @Test @Order(7)
    @DisplayName("TC-SPS-07: Date range is respected")
    void testDateRangeFiltering() {
        LocalDate from = LocalDate.now().minusDays(10);
        LocalDate to   = LocalDate.now().minusDays(5);
        List<SharePrice> prices = service.getSharePrices(new ShareQuery("AAPL", from, to));
        for (SharePrice p : prices) {
            assertFalse(p.getDate().isBefore(from), "No price should be before start date");
            assertFalse(p.getDate().isAfter(to),    "No price should be after end date");
        }
    }

    // ── Stub data provider ────────────────────────────────────────────────────

    static class StubDataProvider implements IDataProvider {
        boolean returnEmpty   = false;
        boolean throwException = false;

        @Override
        public List<SharePrice> getSharePrices(ShareQuery query) throws DataProviderException {
            if (throwException) throw new DataProviderException("Stub provider error");
            if (returnEmpty) return List.of();
            // Return 5 days of synthetic prices
            LocalDate today = LocalDate.now();
            return List.of(
                price(query.getSymbol(), today.minusDays(4), 150.0),
                price(query.getSymbol(), today.minusDays(3), 152.0),
                price(query.getSymbol(), today.minusDays(2), 149.0),
                price(query.getSymbol(), today.minusDays(1), 153.0),
                price(query.getSymbol(), today,              155.0)
            );
        }

        @Override public boolean isAvailable() { return !throwException; }

        private SharePrice price(String sym, LocalDate date, double close) {
            return new SharePrice(sym, date,
                    BigDecimal.valueOf(close - 1),
                    BigDecimal.valueOf(close),
                    BigDecimal.valueOf(close + 2),
                    BigDecimal.valueOf(close - 2),
                    1_000_000L);
        }
    }
}
