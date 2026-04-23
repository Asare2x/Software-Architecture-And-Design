package com.example.trading.soa;

import java.time.LocalDateTime;

/**
 * Service health monitoring for SOA implementation
 */
public class ServiceHealth {
    
    private final String serviceName;
    private final boolean isHealthy;
    private final String status;
    private final LocalDateTime lastChecked;
    private final String errorMessage;
    private final long responseTimeMs;
    
    public ServiceHealth(String serviceName, boolean isHealthy, String status, String errorMessage, long responseTimeMs) {
        this.serviceName = serviceName;
        this.isHealthy = isHealthy;
        this.status = status;
        this.lastChecked = LocalDateTime.now();
        this.errorMessage = errorMessage;
        this.responseTimeMs = responseTimeMs;
    }
    
    public static ServiceHealth healthy(String serviceName) {
        return new ServiceHealth(serviceName, true, "Healthy", null, 0);
    }
    
    public static ServiceHealth unhealthy(String serviceName, String errorMessage) {
        return new ServiceHealth(serviceName, false, "Unhealthy", errorMessage, 0);
    }
    
    public static ServiceHealth degraded(String serviceName, String reason, long responseTime) {
        return new ServiceHealth(serviceName, true, "Degraded", reason, responseTime);
    }
    
    // Getters
    public String getServiceName() { return serviceName; }
    public boolean isHealthy() { return isHealthy; }
    public String getStatus() { return status; }
    public LocalDateTime getLastChecked() { return lastChecked; }
    public String getErrorMessage() { return errorMessage; }
    public long getResponseTimeMs() { return responseTimeMs; }
    
    @Override
    public String toString() {
        return "ServiceHealth{" +
                "serviceName='" + serviceName + '\'' +
                ", isHealthy=" + isHealthy +
                ", status='" + status + '\'' +
                ", lastChecked=" + lastChecked +
                ", errorMessage='" + errorMessage + '\'' +
                ", responseTimeMs=" + responseTimeMs +
                '}';
    }
}
