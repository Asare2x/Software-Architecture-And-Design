package com.shareanalysis.api;

import com.shareanalysis.exception.ServiceException;
import com.shareanalysis.model.SharePrice;
import com.shareanalysis.model.ShareQuery;
import com.shareanalysis.service.SharePriceService;
import com.shareanalysis.service.AccountWalletService;

import java.time.LocalDate;
import java.util.List;

/**
 * Component: API
 * Directions:
 *   API ← SharePriceService      (exposes price endpoints)
 *   API ← Account and Wallet Service (exposes account endpoints)
 *
 * Simulates an API layer that exposes the application's features as endpoints.
 * In Sprint 3 this will be replaced with a real REST controller (e.g. Spring MVC).
 * For Sprint 2 it provides a clean interface for the UI to call.
 */
public class ApiService {

    private final SharePriceService    sharePriceService;
    private final AccountWalletService accountWalletService;

    public ApiService(SharePriceService sharePriceService,
                      AccountWalletService accountWalletService) {
        this.sharePriceService    = sharePriceService;
        this.accountWalletService = accountWalletService;
    }

    /**
     * Price endpoint: GET /prices?symbol=AAPL&from=2024-01-01&to=2024-03-31
     */
    public List<SharePrice> getPrices(String symbol, LocalDate from, LocalDate to)
            throws ServiceException {
        System.out.println("[API] GET /prices?symbol=" + symbol);
        ShareQuery query = new ShareQuery(symbol, from, to);
        return sharePriceService.getPrices(query);
    }

    /**
     * Account endpoint: GET /account?id=ACC-001
     */
    public String getAccount(String accountId) {
        System.out.println("[API] GET /account?id=" + accountId);
        return accountWalletService.getAccount(accountId)
                .map(Object::toString)
                .orElse("Account not found: " + accountId);
    }
}
