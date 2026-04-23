package com.example.trading.pipeline;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import com.example.trading.api.IDataProvider;
import com.example.trading.api.IChartData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Data processing pipeline — applies transformations to raw price data
 * before it reaches the chart or service layer.
 */
public class DataProcessingPipeline {

    private final IDataProvider dataProvider;
    private final IChartData    chartData;

    public DataProcessingPipeline(IDataProvider dataProvider, IChartData chartData) {
        this.dataProvider = dataProvider;
        this.chartData    = chartData;
    }

    /** Fetch and sort prices ascending by date. */
    public List<SharePrice> process(ShareQuery query) {
        List<SharePrice> raw = dataProvider.getSharePrices(query);
        return raw.stream()
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
    }

    /** Normalised series: first point = 100, used for multi-symbol comparison. */
    public List<double[]> processNormalised(ShareQuery query) {
        List<SharePrice> prices = process(query);
        if (prices.isEmpty()) return List.of();
        double base = prices.get(0).getClosePriceAsDouble();
        if (base == 0) base = 1;
        final double b = base;
        return prices.stream()
                .map(p -> new double[]{
                        (double) p.getDate().toEpochDay(),
                        (p.getClosePriceAsDouble() / b) * 100.0
                })
                .collect(Collectors.toList());
    }

    /** Chart-ready data via IChartData contract. */
    public List<SharePrice> processForChart(ShareQuery query) {
        return chartData.getChartData(query);
    }
}
