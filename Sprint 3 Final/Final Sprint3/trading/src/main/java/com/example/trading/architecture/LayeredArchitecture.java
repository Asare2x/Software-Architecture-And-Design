package com.example.trading.architecture;

/**
 * Defines the layered architecture structure for the trading application
 * Following Domain-Independent Layered Pattern
 */
public interface LayeredArchitecture {
    
    /**
     * Presentation Layer - UI Components and Controllers
     */
    interface PresentationLayer {
        void handleUserInteraction();
        void updateView();
        void displayData();
    }
    
    /**
     * Business Logic Layer - Service implementations
     */
    interface BusinessLayer {
        void processBusinessLogic();
        void validateBusinessRules();
        void executeBusinessOperations();
    }
    
    /**
     * Data Access Layer - Repository patterns
     */
    interface DataAccessLayer {
        void persistData();
        void retrieveData();
        void updateData();
        void deleteData();
    }
    
    /**
     * Integration Layer - External API communications
     */
    interface IntegrationLayer {
        void communicateWithExternalSystems();
        void transformData();
        void handleExternalResponses();
    }
}
