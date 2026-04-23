package com.example.trading.api;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import java.util.List;

/**
 * Chart data contract used by Chartview to retrieve renderable price series.
 * Decouples the chart component from the data layer.
 */
public interface IChartData {
    List<SharePrice> getChartData(ShareQuery query);
    List<String>     getAvailableSymbols();
}
