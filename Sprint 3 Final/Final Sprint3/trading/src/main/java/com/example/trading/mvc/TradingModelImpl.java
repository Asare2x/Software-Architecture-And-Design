package com.example.trading.mvc;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.service.IPriceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MVC — CONCRETE MODEL
 *
 * Holds the current trading state and drives all registered views.
 * Uses IPriceService to fetch data, keeping it decoupled from
 * any specific data provider.
 */
public class TradingModelImpl implements TradingModel {

    private final IPriceService         priceService;
    private final List<TradingView>     views        = new ArrayList<>();
    private List<SharePrice>            currentPrices = new ArrayList<>();
    private String                      currentSymbol;

    public TradingModelImpl(IPriceService priceService) {
        this.priceService = priceService;
    }

    @Override
    public void loadPrices(ShareQuery query) {
        this.currentSymbol = query.getSymbol();
        notifyViews(); // tell views loading has begun (prices are empty)

        try {
            this.currentPrices = priceService.getSharePrices(query);
            currentPrices.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        } catch (Exception e) {
            this.currentPrices = Collections.emptyList();
        }
        notifyViews();
    }

    @Override
    public List<SharePrice> getCurrentPrices() {
        return Collections.unmodifiableList(currentPrices);
    }

    @Override
    public String getCurrentSymbol() {
        return currentSymbol;
    }

    @Override
    public SharePrice getLatestPrice() {
        if (currentPrices.isEmpty()) return null;
        return currentPrices.get(currentPrices.size() - 1);
    }

    @Override
    public void addView(TradingView view) {
        if (!views.contains(view)) views.add(view);
    }

    @Override
    public void removeView(TradingView view) {
        views.remove(view);
    }

    @Override
    public void notifyViews() {
        for (TradingView v : views) {
            try { v.onPricesUpdated(currentPrices); }
            catch (Exception ignored) {}
        }
    }

    @Override
    public void clear() {
        currentPrices = new ArrayList<>();
        currentSymbol = null;
        notifyViews();
    }
}
