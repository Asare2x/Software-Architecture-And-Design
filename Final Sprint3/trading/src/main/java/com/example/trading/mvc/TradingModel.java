package com.example.trading.mvc;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.util.List;

/**
 * MVC — MODEL
 *
 * Defines the data contract for the trading view model.
 * The Model holds application state and business data,
 * completely independent of how it is displayed.
 */
public interface TradingModel {

    /** Load prices for the given query into the model */
    void loadPrices(ShareQuery query);

    /** Return the currently loaded prices */
    List<SharePrice> getCurrentPrices();

    /** Return the currently selected symbol */
    String getCurrentSymbol();

    /** Return the latest single price for the current symbol */
    SharePrice getLatestPrice();

    /** Register a view to be notified on data changes */
    void addView(TradingView view);

    /** Unregister a view */
    void removeView(TradingView view);

    /** Notify all registered views that data has changed */
    void notifyViews();

    /** Clear current data */
    void clear();
}
