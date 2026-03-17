# Share Price Technical Analysis — Sprint 2

## How to open in IntelliJ

1. Unzip this folder
2. Open IntelliJ IDEA
3. Click **Open** (or File > Open)
4. Select the **sprint2** folder (the one containing pom.xml)
5. IntelliJ will detect it as a Maven project — click **Trust Project** if prompted
6. Wait for Maven to finish indexing (bottom status bar)
7. Open **src/main/java/com/shareanalysis/Main.java**
8. Click the green **Run** arrow next to the main() method

## Project Structure (Clean Architecture)

```
src/main/java/com/shareanalysis/
│
├── Main.java                          ← Entry point — run this
├── ApplicationContext.java            ← Wires all layers together
│
├── domain/                            ← Core business logic (no dependencies)
│   ├── model/
│   │   ├── SharePrice.java            ← Daily OHLCV price record
│   │   ├── ShareQuery.java            ← User's search request
│   │   ├── ComparisonResult.java      ← Result of comparing 2 companies
│   │   └── Alert.java                 ← Price alert configuration
│   ├── repository/
│   │   ├── ShareRepository.java       ← Interface for local data storage
│   │   └── AlertRepository.java       ← Interface for alert storage
│   ├── service/
│   │   ├── SharePriceService.java     ← Business logic interface
│   │   └── AlertService.java          ← Alert logic interface
│   └── exception/
│       ├── DomainException.java
│       ├── ServiceException.java
│       └── DataProviderException.java
│
├── application/                       ← Use case logic
│   ├── port/
│   │   ├── GetSharePricesUseCase.java ← Use Case 1 interface
│   │   ├── CompareSharesUseCase.java  ← Use Case 3 interface
│   │   └── ManageAlertsUseCase.java   ← Use Case 5 interface
│   └── usecase/
│       ├── GetSharePricesUseCaseImpl.java
│       ├── CompareSharesUseCaseImpl.java
│       ├── ManageAlertsUseCaseImpl.java
│       └── SharePriceServiceImpl.java
│
└── infrastructure/                    ← External adapters
    ├── api/
    │   ├── ShareDataProvider.java     ← API interface
    │   └── YahooFinanceProvider.java  ← Yahoo Finance adapter (stub)
    ├── persistence/
    │   ├── JsonShareRepository.java   ← Local JSON cache
    │   └── InMemoryAlertRepository.java
    └── ui/
        └── ConsoleView.java           ← Console UI
```

## What it does when you run it

1. Retrieves 3 months of AAPL price data (stub/synthetic)
2. Prints a summary table and 20-day SMA
3. Compares AAPL vs MSFT for January 2024
4. Creates a price alert for AAPL above £185
