package com.example.trading.architecture;

/**
 * N-TIERED ARCHITECTURE DOCUMENTATION
 *
 * This trading system follows a 5-tier N-tiered architecture:
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  TIER 1 — PRESENTATION                                          │
 * │  LoginController, TradingDashboard, TradingController (FXML)    │
 * │  Compound components: StockInfoPanel, AlertPanel,               │
 * │  WatchlistPanel, PriceTickerPanel                               │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  TIER 2 — APPLICATION / MVC CONTROLLER                         │
 * │  mvc.TradingModelImpl, mvc.TradingController, mvc.TradingView   │
 * │  Translates user intent into service calls                      │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  TIER 3 — BUSINESS LOGIC / SERVICES                            │
 * │  SharePriceService, AlertService, PerformanceService,           │
 * │  AccountWalletService, PriceComparisonService, ChartingService  │
 * │  All exposed via interfaces (IPriceService, IAlert, etc.)       │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  TIER 4 — DATA ACCESS / REPOSITORY                             │
 * │  JsonSharePriceRepository, InMemoryAccountRepository            │
 * │  Abstractions: IAccountRepository, SharePriceRepository         │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  TIER 5 — INTEGRATION / EXTERNAL                               │
 * │  YahooFinanceProvider → IAPI                                    │
 * │  MarketDataAdapter wraps IAPI into IDataProvider                │
 * │  DataProcessingPipeline applies Filters before data is returned │
 * └─────────────────────────────────────────────────────────────────┘
 *
 * Each tier depends ONLY on the tier below it (via interfaces),
 * never on the tier above — enforcing strict downward dependency flow.
 *
 * Cross-cutting concerns:
 *   - soa.ServiceRegistry    (service discovery across all tiers)
 *   - blackboard.MarketDataBlackboard (shared state across Tier 3)
 *   - exception package      (shared error model)
 *   - pipeline/Filters       (Pipes & Filters within Tier 5→4 boundary)
 */
public interface NTieredArchitecture {

    interface PresentationTier  extends LayeredArchitecture.PresentationLayer {}
    interface ApplicationTier   extends LayeredArchitecture.BusinessLayer {}
    interface BusinessTier      extends LayeredArchitecture.BusinessLayer {}
    interface DataAccessTier    extends LayeredArchitecture.DataAccessLayer {}
    interface IntegrationTier   extends LayeredArchitecture.IntegrationLayer {}
}
