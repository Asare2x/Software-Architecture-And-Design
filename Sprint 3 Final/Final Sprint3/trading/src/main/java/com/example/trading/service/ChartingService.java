package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
public class ChartingService {
    private final IPriceService sharePriceService;
    
    public ChartingService(IPriceService sharePriceService) {
        this.sharePriceService = sharePriceService;
    }
    
    /**
     * Create a line chart for a single symbol
     */
    public LineChart<Number, Number> createSingleChart(String symbol, LocalDate startDate, LocalDate endDate) {
        validateParameters(symbol, startDate, endDate);
        
        try {
            List<SharePrice> prices = sharePriceService.getSharePrices(
                new ShareQuery(symbol, startDate, endDate));
            
            if (prices.isEmpty()) {
                throw new ServiceException("No data available for chart");
            }
            
            // Create axes
            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Date");
            yAxis.setLabel("Price ($)");
            
            // Create chart
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle(symbol + " Price Chart");
            lineChart.setCreateSymbols(false);
            
            // Create data series
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(symbol);
            
            // Sort prices by date
            prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
            
            // Add data points
            for (int i = 0; i < prices.size(); i++) {
                SharePrice price = prices.get(i);
                series.getData().add(new XYChart.Data<>(i, price.getClosePrice().doubleValue()));
            }
            
            lineChart.getData().add(series);
            return lineChart;
            
        } catch (Exception e) {
            throw new ServiceException("Error creating chart", e);
        }
    }
    
    /**
     * Create a comparison chart for two symbols
     */
    public LineChart<Number, Number> createComparisonChart(String symbol1, String symbol2, 
                                                          LocalDate startDate, LocalDate endDate) {
        validateParameters(symbol1, startDate, endDate);
        validateParameters(symbol2, startDate, endDate);
        
        try {
            List<SharePrice> prices1 = sharePriceService.getSharePrices(
                new ShareQuery(symbol1, startDate, endDate));
            List<SharePrice> prices2 = sharePriceService.getSharePrices(
                new ShareQuery(symbol2, startDate, endDate));
            
            if (prices1.isEmpty() || prices2.isEmpty()) {
                throw new ServiceException("Insufficient data for comparison chart");
            }
            
            // Create axes
            NumberAxis xAxis = new NumberAxis();
            NumberAxis yAxis = new NumberAxis();
            xAxis.setLabel("Date");
            yAxis.setLabel("Normalized Price (%)");
            
            // Create chart
            LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
            lineChart.setTitle(symbol1 + " vs " + symbol2 + " Comparison");
            lineChart.setCreateSymbols(false);
            
            // Create normalized series
            XYChart.Series<Number, Number> series1 = createNormalizedSeries(symbol1, prices1);
            XYChart.Series<Number, Number> series2 = createNormalizedSeries(symbol2, prices2);
            
            lineChart.getData().addAll(series1, series2);
            return lineChart;
            
        } catch (Exception e) {
            throw new ServiceException("Error creating comparison chart", e);
        }
    }
    
    /**
     * Create a normalized price series (percentage change from start)
     */
    private XYChart.Series<Number, Number> createNormalizedSeries(String symbol, List<SharePrice> prices) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(symbol);
        
        // Sort by date
        prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
        
        if (prices.isEmpty()) {
            return series;
        }
        
        SharePrice firstPrice = prices.get(0);
        if (firstPrice == null || firstPrice.getClosePrice() == null) {
            throw new ServiceException("Invalid price data: null price found");
        }
        
        BigDecimal basePrice = firstPrice.getClosePrice();
        
        // Validate base price
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Invalid base price for normalization: " + basePrice);
        }
        
        for (int i = 0; i < prices.size(); i++) {
            SharePrice price = prices.get(i);
            BigDecimal currentPrice = price.getClosePrice();
            BigDecimal normalizedValue = currentPrice
                .subtract(basePrice)
                .divide(basePrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
                
            series.getData().add(new XYChart.Data<>(i, normalizedValue.doubleValue()));
        }
        
        return series;
    }
    
    /**
     * Get chart data as a formatted string for export
     */
    public String exportChartData(String symbol, LocalDate startDate, LocalDate endDate) {
        validateParameters(symbol, startDate, endDate);
        
        try {
            List<SharePrice> prices = sharePriceService.getSharePrices(
                new ShareQuery(symbol, startDate, endDate));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Date,Open,High,Low,Close,Volume\n");
            
            prices.sort((p1, p2) -> p1.getDate().compareTo(p2.getDate()));
            
            for (SharePrice price : prices) {
                sb.append(price.getDate()).append(",")
                  .append(price.getOpenPrice()).append(",")
                  .append(price.getHighPrice()).append(",")
                  .append(price.getLowPrice()).append(",")
                  .append(price.getClosePrice()).append(",")
                  .append(price.getVolume()).append("\n");
            }
            
            return sb.toString();
            
        } catch (Exception e) {
            throw new ServiceException("Error exporting chart data", e);
        }
    }
    
    /**
     * Validate method parameters
     */
    private void validateParameters(String symbol, LocalDate startDate, LocalDate endDate) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
