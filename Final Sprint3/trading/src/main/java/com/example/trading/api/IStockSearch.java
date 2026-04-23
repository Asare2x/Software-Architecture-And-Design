package com.example.trading.api;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.util.List;

/**
 * Stock search contract — used by StockSearchComponent (via ConsoleView/TradingDashboard).
 * Separates search logic from the UI layer.
 */
public interface IStockSearch {
    List<SharePrice> search(ShareQuery query);
    List<String>     suggestSymbols(String prefix);
    boolean          isValidSymbol(String symbol);
}
