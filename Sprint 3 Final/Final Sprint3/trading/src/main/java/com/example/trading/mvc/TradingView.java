package com.example.trading.mvc;

import com.example.trading.model.SharePrice;

import java.util.List;

/**
 * MVC — VIEW
 *
 * The View is purely responsible for displaying data.
 * It receives updates from the Model and renders them.
 * It knows nothing about business logic or data retrieval.
 */
public interface TradingView {

    /** Called by the Model when price data has been refreshed */
    void onPricesUpdated(List<SharePrice> prices);

    /** Called when a loading operation begins */
    void onLoadingStarted(String symbol);

    /** Called when a loading operation completes or fails */
    void onLoadingFinished();

    /** Called when an error occurs that the view should display */
    void onError(String message);

    /** Returns the name of this view for logging/debugging */
    String getViewName();
}
