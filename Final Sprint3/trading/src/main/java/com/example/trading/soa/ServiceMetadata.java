package com.example.trading.soa;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Service metadata for SOA implementation
 * Provides service information and interoperability details
 */
public class ServiceMetadata {
    
    private final String serviceName;
    private final String version;
    private final String description;
    private final LocalDateTime startTime;
    private final String owner;
    private final Map<String, String> endpoints;
    private final Map<String, Object> configuration;
    
    public ServiceMetadata(String serviceName, String version, String description, 
                          String owner, Map<String, String> endpoints, 
                          Map<String, Object> configuration) {
        this.serviceName = serviceName;
        this.version = version;
        this.description = description;
        this.startTime = LocalDateTime.now();
        this.owner = owner;
        this.endpoints = endpoints != null ? Map.copyOf(endpoints) : Collections.emptyMap();
        this.configuration = configuration != null ? Map.copyOf(configuration) : Collections.emptyMap();
    }
    
    // Factory methods for common service types
    public static ServiceMetadata createTradingService(String version) {
        return new ServiceMetadata(
            "TradingService",
            version,
            "Core trading service providing stock price data and analysis",
            "Trading System",
            Map.of(
                "getPrices", "/api/prices",
                "getLatestPrice", "/api/prices/latest",
                "healthCheck", "/api/health"
            ),
            Map.of(
                "maxRequestsPerMinute", 1000,
                "timeoutMs", 30000,
                "retryAttempts", 3
            )
        );
    }
    
    public static ServiceMetadata createAuthService(String version) {
        return new ServiceMetadata(
            "AuthenticationService",
            version,
            "User authentication and session management service",
            "Security Module",
            Map.of(
                "login", "/api/auth/login",
                "logout", "/api/auth/logout",
                "validate", "/api/auth/validate"
            ),
            Map.of(
                "sessionTimeoutMinutes", 30,
                "maxLoginAttempts", 5,
                "passwordComplexity", "high"
            )
        );
    }
    
    public static ServiceMetadata createDataProviderService(String provider, String version) {
        return new ServiceMetadata(
            provider + "DataProvider",
            version,
            "External data provider integration service for " + provider,
            "Integration Layer",
            Map.of(
                "getData", "/api/external/" + provider.toLowerCase(),
                "status", "/api/external/" + provider.toLowerCase() + "/status"
            ),
            Map.of(
                "provider", provider,
                "rateLimitPerHour", 10000,
                "cacheTtlSeconds", 300
            )
        );
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public String getVersion() { return version; }
    public String getDescription() { return description; }
    public LocalDateTime getStartTime() { return startTime; }
    public String getOwner() { return owner; }
    public Map<String, String> getEndpoints() { return endpoints; }
    public Map<String, Object> getConfiguration() { return configuration; }
    
    /**
     * Get uptime in seconds
     */
    public long getUptimeSeconds() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }
    
    /**
     * Get formatted uptime string
     */
    public String getFormattedUptime() {
        long seconds = getUptimeSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
    
    /**
     * Check if service supports a specific endpoint
     */
    public boolean hasEndpoint(String endpointName) {
        return endpoints.containsKey(endpointName);
    }
    
    /**
     * Get endpoint URL
     */
    public String getEndpoint(String endpointName) {
        return endpoints.get(endpointName);
    }
    
    /**
     * Get configuration value
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigurationValue(String key, Class<T> type) {
        Object value = configuration.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "ServiceMetadata{" +
                "serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", owner='" + owner + '\'' +
                ", uptime='" + getFormattedUptime() + '\'' +
                ", endpointCount=" + endpoints.size() +
                '}';
    }
}
