# Software-Architecture-And-Design

## Share Price Analysis System  
Sprint 1 – Architecture & Component Design

This project is a Java-based Share Price Analysis System designed using a layered architecture.

The system allows users to:
- Enter a ticker symbol
- Select a date range
- Retrieve historical share price data
- Cache results locally
- Fetch live market data from the Yahoo Finance API

Sprint 1 focused on defining the system requirements, designing the architecture, and creating an abstract implementation structure.

---

## Architecture Overview

The system follows a layered design:

UI Layer → Application Core → Infrastructure → External Systems

### UI Layer
- ConsoleView  
Handles user interaction and sends requests to the service layer.

### Application Core
- ISharePriceService  
- SharePriceServiceImpl  
- ApplicationContext  
- Domain Model (SharePrice, ShareQuery)

Contains the main business logic and coordinates data retrieval.

### Infrastructure
- IShareRepository → JsonShareRepository  
- ShareDataProvider → YahooFinanceProvider  

Manages caching, file storage, and communication with external systems.

### External Systems
- Yahoo Finance HTTP API  
- Local File System (JSON cache)

---

## Component Relationships

- The UI depends on ISharePriceService.
- The service depends on the repository for cached data and the data provider for live data.
- The repository interacts with the file system.
- The data provider communicates with the Yahoo Finance API.
- The domain model represents core data objects used across the system.
- ApplicationContext wires all components together.

---

## Sprint 1 Deliverables

- Requirements identified and scoped  
- Layered architectural design  
- Component diagram created  
- Abstract Java implementation completed  
- GitHub repository and project board set up
