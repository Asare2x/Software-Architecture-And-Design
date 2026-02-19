# Share Price Technical Analysis — Sprint 1

## Overview

Sprint 1 establishes the architectural skeleton of the Share Price Comparison
web application. The primary goal is to define the key components, their
interfaces, and the data flow between them, implemented abstractly in Java.

---

## Architecture: Simple Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI Layer                                 │
│                      ConsoleView                                │
│        (Sprint 3: replaced with Web front-end)                  │
└──────────────────────────┬──────────────────────────────────────┘
                           │ uses interface
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Service Layer                               │
│              « interface » SharePriceService                    │
│              SharePriceServiceImpl                              │
│   - Cache-first retrieval strategy                              │
│   - SMA / technical analysis calculations                       │
└───────────┬─────────────────────────────────┬───────────────────┘
            │ uses interface                  │ uses interface
            ▼                                 ▼
┌───────────────────────┐       ┌─────────────────────────────────┐
│   External API Layer  │       │       Persistence Layer          │
│ « interface »         │       │ « interface » ShareRepository    │
│ ShareDataProvider     │       │ JsonShareRepository              │
│ YahooFinanceProvider  │       │ (JSON file / in-memory cache)   │
└───────────────────────┘       └─────────────────────────────────┘
            │ fetches from
            ▼
   Yahoo Finance API (external)
```

### Design Decisions

| Decision | Rationale |
|---|---|
| Interface-first design | All layers depend on interfaces, not concrete classes. Enables easy substitution (e.g. swap Yahoo for Alpha Vantage). |
| Cache-first strategy | Fulfils the offline support requirement. Remote fetch only when cache is stale or empty. |
| ShareQuery validation | Enforces the ≤2 year business rule at the domain level, before any network call. |
| Stub data provider | Allows the full stack to run without a live API key during Sprint 1. |

---

## Component Specification

| Component | Interface | Responsibility |
|---|---|---|
| `ConsoleView` | — | UI: collects input, renders output |
| `SharePriceServiceImpl` | `SharePriceService` | Orchestrates fetch, cache, analytics |
| `YahooFinanceProvider` | `ShareDataProvider` | Fetches live prices from Yahoo Finance |
| `JsonShareRepository` | `ShareRepository` | Persists prices locally as JSON |
| `SharePrice` | — | Domain model: one day's OHLCV data |
| `ShareQuery` | — | Value object: validated user request |

---

## Project Structure

```
src/main/java/com/shareanalysis/
├── Main.java                          ← Entry point
├── ApplicationContext.java            ← Dependency wiring
├── model/
│   ├── SharePrice.java                ← Domain model
│   └── ShareQuery.java                ← Value object (validates ≤2yr range)
├── api/
│   ├── ShareDataProvider.java         ← Interface: external data
│   ├── YahooFinanceProvider.java      ← Stub impl (Sprint 2: real HTTP)
│   └── DataProviderException.java
├── repository/
│   ├── ShareRepository.java           ← Interface: local storage
│   └── JsonShareRepository.java       ← JSON / in-memory impl
├── service/
│   ├── SharePriceService.java         ← Interface: business logic
│   ├── SharePriceServiceImpl.java     ← Concrete: cache-first strategy
│   └── ServiceException.java
└── ui/
    └── ConsoleView.java               ← CLI presentation layer
```

---

## How to Run

### Prerequisites
- Java 17+
- No external libraries required for Sprint 1

### Compile

```bash
find src -name "*.java" > sources.txt
javac -d out @sources.txt
```

### Run

```bash
java -cp out com.shareanalysis.Main
```

---

## Sprint Roadmap

| Sprint | Focus |
|---|---|
| Sprint 1 (current) | Simple Architecture, component interfaces, abstract stub |
| Sprint 2 | Clean Architecture, real Yahoo Finance HTTP client, UML models |
| Sprint 3 | Compound components, SOA, domain-independent styles, web UI |
