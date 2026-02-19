package com.shareanalysis;

/**
 * Main entry point for the Share Price Technical Analysis application.
 *
 * Sprint 1: Introduction to Architectural Principles
 *
 * This class bootstraps the application and demonstrates the abstract
 * component structure defined in Sprint 1.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Share Price Technical Analysis App ===");
        System.out.println("Sprint 1: Architectural Skeleton Loaded");

        // Demonstrate component wiring (abstract at this stage)
        ApplicationContext context = new ApplicationContext();
        context.initialise();
        context.run();
    }
}
