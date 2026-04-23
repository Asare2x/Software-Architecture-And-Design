package com.example.trading.mvc;

import java.time.LocalDate;

/**
 * MVC — CONTROLLER
 *
 * The Controller handles all user interactions.
 * It translates user input into Model operations.
 * It knows about both the Model and the View but
 * keeps them fully decoupled from each other.
 */
public interface TradingController {

    /** User requested a stock search */
    void onSearchRequested(String symbol, LocalDate from, LocalDate to);

    /** User requested a stock comparison */
    void onCompareRequested(String symbol1, String symbol2, LocalDate from, LocalDate to);

    /** User requested a performance report */
    void onPerformanceRequested(String symbol, LocalDate from, LocalDate to);

    /** User clicked logout */
    void onLogoutRequested();

    /** User changed the date range */
    void onDateRangeChanged(LocalDate from, LocalDate to);

    /** Bind a Model and View to this controller */
    void bind(TradingModel model, TradingView view);
}
