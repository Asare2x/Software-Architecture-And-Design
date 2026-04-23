package com.example.trading.soa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service Registry for SOA implementation
 * Provides service discovery and loose coupling
 */
public class ServiceRegistry {
    
    private static ServiceRegistry instance;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    private final Map<String, Object> servicesByName = new ConcurrentHashMap<>();
    
    private ServiceRegistry() {}
    
    public static ServiceRegistry getInstance() {
        if (instance == null) {
            synchronized (ServiceRegistry.class) {
                if (instance == null) {
                    instance = new ServiceRegistry();
                }
            }
        }
        return instance;
    }
    
    /**
     * Register a service by its interface type
     */
    public <T> void registerService(Class<T> serviceInterface, T serviceImplementation) {
        if (serviceInterface == null || serviceImplementation == null) {
            throw new IllegalArgumentException("Service interface and implementation cannot be null");
        }
        services.put(serviceInterface, serviceImplementation);
        System.out.println("Registered service: " + serviceInterface.getSimpleName());
    }
    
    /**
     * Register a service by name
     */
    public void registerService(String serviceName, Object serviceImplementation) {
        if (serviceName == null || serviceImplementation == null) {
            throw new IllegalArgumentException("Service name and implementation cannot be null");
        }
        servicesByName.put(serviceName, serviceImplementation);
        System.out.println("Registered service by name: " + serviceName);
    }
    
    /**
     * Get service by interface type
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceInterface) {
        return (T) services.get(serviceInterface);
    }
    
    /**
     * Get service by name
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(String serviceName, Class<T> expectedType) {
        Object service = servicesByName.get(serviceName);
        if (service != null && expectedType.isInstance(service)) {
            return expectedType.cast(service);
        }
        return null;
    }
    
    /**
     * Check if service is registered
     */
    public boolean isServiceRegistered(Class<?> serviceInterface) {
        return services.containsKey(serviceInterface);
    }
    
    /**
     * Check if service is registered by name
     */
    public boolean isServiceRegistered(String serviceName) {
        return servicesByName.containsKey(serviceName);
    }
    
    /**
     * Unregister service
     */
    public void unregisterService(Class<?> serviceInterface) {
        services.remove(serviceInterface);
        System.out.println("Unregistered service: " + serviceInterface.getSimpleName());
    }
    
    /**
     * Unregister service by name
     */
    public void unregisterService(String serviceName) {
        servicesByName.remove(serviceName);
        System.out.println("Unregistered service by name: " + serviceName);
    }
    
    /**
     * Get all registered services
     */
    public Map<String, Object> getAllServices() {
        Map<String, Object> allServices = new HashMap<>();
        
        // Add services registered by type
        for (Map.Entry<Class<?>, Object> entry : services.entrySet()) {
            allServices.put(entry.getKey().getSimpleName(), entry.getValue());
        }
        
        // Add services registered by name
        allServices.putAll(servicesByName);
        
        return allServices;
    }
    
    /**
     * Clear all registered services
     */
    public void clearAll() {
        services.clear();
        servicesByName.clear();
        System.out.println("Cleared all registered services");
    }
}
