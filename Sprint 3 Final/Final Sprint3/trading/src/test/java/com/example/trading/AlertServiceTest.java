package com.example.trading;

import com.example.trading.model.Alert;
import com.example.trading.repository.InMemoryAccountRepository;
import com.example.trading.service.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for AlertService.
 *
 * Tests cover:
 *  TC-ALT-01  Create alert returns non-null alert with correct fields
 *  TC-ALT-02  Active alerts retrieved correctly
 *  TC-ALT-03  Alert triggered when price crosses PRICE_ABOVE threshold
 *  TC-ALT-04  Alert NOT triggered when price is below PRICE_ABOVE threshold
 *  TC-ALT-05  Cancel alert changes status
 *  TC-ALT-06  Delete alert removes it from all lists
 *  TC-ALT-07  Get alerts for specific symbol
 *  TC-ALT-08  Alert statistics reflect correct counts
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        AuthenticationService.getInstance().authenticate("admin", "admin123");
        InMemoryAccountRepository repo = new InMemoryAccountRepository();
        AccountWalletService wallet = new AccountWalletService(repo,
                AuthenticationService.getInstance());
        // Use a stub price service that just returns empty — alerts use checkAlerts()
        alertService = new AlertService(wallet,
                new StubPriceService(), repo);
    }

    @AfterEach
    void tearDown() { AuthenticationService.getInstance().logout(); }

    // TC-ALT-01
    @Test @Order(1)
    @DisplayName("TC-ALT-01: Create alert returns correct fields")
    void testCreateAlert() {
        Alert alert = alertService.createAlert("AAPL", new BigDecimal("200.00"),
                Alert.AlertType.PRICE_ABOVE);
        assertNotNull(alert);
        assertEquals("AAPL", alert.getSymbol());
        assertEquals(0, new BigDecimal("200.00").compareTo(alert.getTargetPrice()));
        assertEquals(Alert.AlertType.PRICE_ABOVE, alert.getAlertType());
        assertTrue(alert.isActive());
    }

    // TC-ALT-02
    @Test @Order(2)
    @DisplayName("TC-ALT-02: Active alerts are retrievable")
    void testGetActiveAlerts() {
        alertService.createAlert("MSFT", new BigDecimal("400.00"), Alert.AlertType.PRICE_ABOVE);
        alertService.createAlert("TSLA", new BigDecimal("200.00"), Alert.AlertType.PRICE_BELOW);
        List<Alert> active = alertService.getActiveAlerts();
        assertTrue(active.size() >= 2);
        active.forEach(a -> assertTrue(a.isActive()));
    }

    // TC-ALT-03
    @Test @Order(3)
    @DisplayName("TC-ALT-03: PRICE_ABOVE alert triggers when price exceeds target")
    void testPriceAboveTriggered() {
        Alert alert = alertService.createAlert("AAPL", new BigDecimal("150.00"),
                Alert.AlertType.PRICE_ABOVE);
        // Manually check against a price above target
        boolean fired = alert.check(new BigDecimal("155.00"));
        assertTrue(fired, "Alert should trigger when current price > target");
        assertFalse(alert.isActive(), "Alert should no longer be active after triggering");
    }

    // TC-ALT-04
    @Test @Order(4)
    @DisplayName("TC-ALT-04: PRICE_ABOVE alert does NOT trigger when price is below target")
    void testPriceAboveNotTriggered() {
        Alert alert = alertService.createAlert("AAPL", new BigDecimal("200.00"),
                Alert.AlertType.PRICE_ABOVE);
        boolean fired = alert.check(new BigDecimal("190.00"));
        assertFalse(fired, "Alert should NOT trigger when current price < target");
        assertTrue(alert.isActive(), "Alert should still be active");
    }

    // TC-ALT-05
    @Test @Order(5)
    @DisplayName("TC-ALT-05: Cancelled alert is removed from active list")
    void testCancelAlert() {
        Alert alert = alertService.createAlert("GOOGL", new BigDecimal("170.00"),
                Alert.AlertType.PRICE_ABOVE);
        String id = alert.getAlertId();
        alertService.cancelAlert(id);
        List<Alert> active = alertService.getActiveAlerts();
        boolean stillActive = active.stream().anyMatch(a -> a.getAlertId().equals(id));
        assertFalse(stillActive, "Cancelled alert should not appear in active list");
    }

    // TC-ALT-06
    @Test @Order(6)
    @DisplayName("TC-ALT-06: Deleted alert is removed from all lists")
    void testDeleteAlert() {
        Alert alert = alertService.createAlert("AMZN", new BigDecimal("190.00"),
                Alert.AlertType.PRICE_BELOW);
        String id = alert.getAlertId();
        alertService.deleteAlert(id);
        List<Alert> all = alertService.getAllAlerts();
        boolean found = all.stream().anyMatch(a -> a.getAlertId().equals(id));
        assertFalse(found, "Deleted alert should not appear in any list");
    }

    // TC-ALT-07
    @Test @Order(7)
    @DisplayName("TC-ALT-07: getAlertsForSymbol returns only matching symbol")
    void testGetAlertsForSymbol() {
        alertService.createAlert("NVDA", new BigDecimal("900.00"), Alert.AlertType.PRICE_ABOVE);
        alertService.createAlert("META", new BigDecimal("500.00"), Alert.AlertType.PRICE_ABOVE);
        List<Alert> nvdaAlerts = alertService.getAlertsForSymbol("NVDA");
        assertTrue(nvdaAlerts.stream().allMatch(a -> a.getSymbol().equals("NVDA")),
                "All returned alerts should be for NVDA");
    }

    // TC-ALT-08
    @Test @Order(8)
    @DisplayName("TC-ALT-08: Alert statistics counts are accurate")
    void testAlertStatistics() {
        alertService.createAlert("AAPL", new BigDecimal("200.00"), Alert.AlertType.PRICE_ABOVE);
        Alert toCancel = alertService.createAlert("MSFT", new BigDecimal("300.00"), Alert.AlertType.PRICE_ABOVE);
        alertService.cancelAlert(toCancel.getAlertId());

        AlertService.AlertStatistics stats = alertService.getAlertStatistics();
        assertTrue(stats.getActiveAlerts()    >= 1, "Should have at least 1 active alert");
        assertTrue(stats.getCancelledAlerts() >= 1, "Should have at least 1 cancelled alert");
        assertEquals(stats.getTotalAlerts(),
                stats.getActiveAlerts() + stats.getTriggeredAlerts() + stats.getCancelledAlerts(),
                "Total should equal sum of all statuses");
    }

    // ── Stub ──────────────────────────────────────────────────────────────────
    static class StubPriceService implements IPriceService {
        @Override public List<com.example.trading.model.SharePrice> getSharePrices(
                com.example.trading.model.ShareQuery q) { return List.of(); }
        @Override public com.example.trading.model.SharePrice getLatestPrice(String s) { return null; }
        @Override public List<String> getAvailableSymbols() { return List.of(); }
        @Override public boolean isDataProviderAvailable() { return false; }
    }
}
