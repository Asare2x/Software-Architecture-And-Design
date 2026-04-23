package com.example.trading.soa;

import com.example.trading.model.SharePrice;
import com.example.trading.model.ShareQuery;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service contract defining trading operations
 * Ensures service modularity and interoperability
 */
public interface TradingServiceContract {
    
    /**
     * Asynchronously retrieve stock prices
     */
    CompletableFuture<List<SharePrice>> getStockPricesAsync(ShareQuery query);
    
    /**
     * Synchronously retrieve stock prices
     */
    List<SharePrice> getStockPrices(ShareQuery query);
    
    /**
     * Get latest price for a symbol
     */
    SharePrice getLatestPrice(String symbol);
    
    /**
     * Validate stock symbol
     */
    boolean isValidSymbol(String symbol);
    
    /**
     * Get service health status
     */
    ServiceHealth getHealthStatus();
    
    /**
     * Get service metadata
     */
    ServiceMetadata getServiceMetadata();
    
    /**
     * Service configuration interface
     */
    interface ServiceConfiguration {
        String getServiceName();
        String getVersion();
        int getTimeoutMs();
        boolean isEnabled();
    }
    
    /**
     * Service monitoring interface
     */
    interface ServiceMonitoring {
        long getRequestCount();
        long getErrorCount();
        double getAverageResponseTime();
        boolean isHealthy();
    }
}
