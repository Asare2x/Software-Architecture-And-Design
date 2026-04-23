package com.example.trading.service;

import com.example.trading.exception.ServiceException;
import com.example.trading.model.Account;
import com.example.trading.repository.IAccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AccountWalletService {
    private final IAccountRepository accountRepository;
    private final IAuthService authService;
    
    public AccountWalletService(IAccountRepository accountRepository, IAuthService authService) {
        this.accountRepository = accountRepository;
        this.authService = authService;
    }
    
    /**
     * Get current user's account
     */
    public Account getCurrentUserAccount() {
        String currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new ServiceException("No user logged in");
        }
        
        Account account = accountRepository.findByUsername(currentUser);
        if (account == null) {
            // Create new account for user
            account = createAccountForUser(currentUser);
        }
        
        return account;
    }
    
    /**
     * Create a new account for a user
     */
    private Account createAccountForUser(String username) {
        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setUsername(username);
        account.setBalance(BigDecimal.ZERO);
        
        return accountRepository.save(account);
    }
    
    /**
     * Add symbol to watchlist
     */
    public void addToWatchlist(String symbol) {
        try {
            Account account = getCurrentUserAccount();
            account.addToWatchlist(symbol.toUpperCase());
            accountRepository.save(account);
        } catch (Exception e) {
            throw new ServiceException("Error adding to watchlist", e);
        }
    }
    
    /**
     * Remove symbol from watchlist
     */
    public void removeFromWatchlist(String symbol) {
        try {
            Account account = getCurrentUserAccount();
            account.removeFromWatchlist(symbol.toUpperCase());
            accountRepository.save(account);
        } catch (Exception e) {
            throw new ServiceException("Error removing from watchlist", e);
        }
    }
    
    /**
     * Get user's watchlist
     */
    public List<String> getWatchlist() {
        Account account = getCurrentUserAccount();
        return account.getWatchlist();
    }
    
    /**
     * Check if symbol is in watchlist
     */
    public boolean isInWatchlist(String symbol) {
        Account account = getCurrentUserAccount();
        return account.getWatchlist().contains(symbol.toUpperCase());
    }
    
    /**
     * Update account balance (for future features)
     */
    public void updateBalance(BigDecimal newBalance) {
        try {
            Account account = getCurrentUserAccount();
            account.setBalance(newBalance);
            accountRepository.save(account);
        } catch (Exception e) {
            throw new ServiceException("Error updating balance", e);
        }
    }
    
    /**
     * Get account balance
     */
    public BigDecimal getBalance() {
        Account account = getCurrentUserAccount();
        return account.getBalance();
    }
    
    /**
     * Get all accounts (admin function)
     */
    public List<Account> getAllAccounts() {
        // Only allow if admin user
        String currentUser = authService.getCurrentUser();
        if (!"admin".equals(currentUser)) {
            throw new ServiceException("Access denied");
        }
        
        return accountRepository.findAll();
    }

    /**
     * Deposit funds into the current user's account
     */
    public BigDecimal deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Deposit amount must be greater than zero");
        }
        Account account = getCurrentUserAccount();
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        return newBalance;
    }

    /**
     * Withdraw funds from the current user's account
     */
    public BigDecimal withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Withdrawal amount must be greater than zero");
        }
        Account account = getCurrentUserAccount();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ServiceException("Insufficient balance. Available: " + account.getBalance());
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);
        return newBalance;
    }

    /**
     * Transfer funds from current user to another user
     */
    public void transfer(String toUsername, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ServiceException("Transfer amount must be greater than zero");
        }
        String currentUser = authService.getCurrentUser();
        if (currentUser == null) throw new ServiceException("No user logged in");
        if (currentUser.equalsIgnoreCase(toUsername)) {
            throw new ServiceException("Cannot transfer to yourself");
        }

        Account fromAccount = getCurrentUserAccount();
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new ServiceException("Insufficient balance for transfer");
        }

        Account toAccount = accountRepository.findByUsername(toUsername);
        if (toAccount == null) {
            throw new ServiceException("Recipient account not found: " + toUsername);
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    /**
     * Check if the current user has sufficient balance
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return getBalance().compareTo(amount) >= 0;
    }

    /**
     * Get account summary as a formatted string
     */
    public String getAccountSummary() {
        Account account = getCurrentUserAccount();
        return String.format(
            "Account ID: %s | User: %s | Balance: $%.2f | Watchlist: %d symbols",
            account.getAccountId(),
            account.getUsername(),
            account.getBalance().doubleValue(),
            account.getWatchlist().size()
        );
    }
}
