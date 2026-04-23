package com.example.trading;

import com.example.trading.model.SharePrice;
import com.example.trading.pipeline.*;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Pipes and Filters pattern.
 *
 * TC-PF-01  SortByDateFilter orders records ascending
 * TC-PF-02  DateRangeFilter excludes out-of-range records
 * TC-PF-03  RemoveInvalidPricesFilter removes zero-price records
 * TC-PF-04  LimitFilter returns only last N records
 * TC-PF-05  DownsampleFilter reduces dataset size
 * TC-PF-06  FilterPipeline chains multiple filters correctly
 * TC-PF-07  Empty input passes through all filters without error
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PipelineFilterTest {

    private List<SharePrice> testData;

    @BeforeEach
    void setUp() {
        testData = new ArrayList<>(List.of(
            price("AAPL", LocalDate.of(2025, 3, 5),  150.0),
            price("AAPL", LocalDate.of(2025, 3, 1),  148.0),
            price("AAPL", LocalDate.of(2025, 3, 8),  0.0),    // invalid
            price("AAPL", LocalDate.of(2025, 2, 28), 145.0),
            price("AAPL", LocalDate.of(2025, 3, 3),  149.0)
        ));
    }

    @Test @Order(1)
    @DisplayName("TC-PF-01: SortByDateFilter orders ascending")
    void testSortByDate() {
        List<SharePrice> result = new Filters.SortByDateFilter().apply(testData);
        for (int i = 1; i < result.size(); i++)
            assertFalse(result.get(i).getDate().isBefore(result.get(i-1).getDate()));
    }

    @Test @Order(2)
    @DisplayName("TC-PF-02: DateRangeFilter excludes out-of-range records")
    void testDateRange() {
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to   = LocalDate.of(2025, 3, 6);
        List<SharePrice> result = new Filters.DateRangeFilter(from, to).apply(testData);
        result.forEach(p -> {
            assertFalse(p.getDate().isBefore(from));
            assertFalse(p.getDate().isAfter(to));
        });
        assertFalse(result.isEmpty());
    }

    @Test @Order(3)
    @DisplayName("TC-PF-03: RemoveInvalidPricesFilter removes zero-price records")
    void testRemoveInvalid() {
        List<SharePrice> result = new Filters.RemoveInvalidPricesFilter().apply(testData);
        result.forEach(p -> assertTrue(p.getClosePriceAsDouble() > 0));
        assertEquals(4, result.size());
    }

    @Test @Order(4)
    @DisplayName("TC-PF-04: LimitFilter returns last N records")
    void testLimit() {
        List<SharePrice> sorted = new Filters.SortByDateFilter().apply(testData);
        List<SharePrice> result = new Filters.LimitFilter(2).apply(sorted);
        assertEquals(2, result.size());
    }

    @Test @Order(5)
    @DisplayName("TC-PF-05: DownsampleFilter reduces dataset size")
    void testDownsample() {
        List<SharePrice> result = new Filters.DownsampleFilter(2).apply(testData);
        assertTrue(result.size() < testData.size());
    }

    @Test @Order(6)
    @DisplayName("TC-PF-06: FilterPipeline chains filters correctly")
    void testPipelineChain() {
        FilterPipeline pipeline = new FilterPipeline()
                .addFilter(new Filters.RemoveInvalidPricesFilter())
                .addFilter(new Filters.SortByDateFilter())
                .addFilter(new Filters.DateRangeFilter(
                        LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 10)))
                .addFilter(new Filters.LimitFilter(3));

        List<SharePrice> result = pipeline.execute(testData);
        assertEquals(3, pipeline.size(), "Pipeline should have 4 filters");
        result.forEach(p -> assertTrue(p.getClosePriceAsDouble() > 0));
    }

    @Test @Order(7)
    @DisplayName("TC-PF-07: Empty input passes through pipeline without error")
    void testEmptyInput() {
        FilterPipeline pipeline = new FilterPipeline()
                .addFilter(new Filters.SortByDateFilter())
                .addFilter(new Filters.RemoveInvalidPricesFilter());
        List<SharePrice> result = pipeline.execute(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    private SharePrice price(String sym, LocalDate date, double close) {
        return new SharePrice(sym, date,
                BigDecimal.valueOf(close > 0 ? close - 1 : 0),
                BigDecimal.valueOf(close),
                BigDecimal.valueOf(close > 0 ? close + 1 : 0),
                BigDecimal.valueOf(close > 0 ? close - 1 : 0),
                500_000L);
    }
}
