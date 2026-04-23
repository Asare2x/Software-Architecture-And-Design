package com.example.trading.repository;

import com.example.trading.model.Account;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountRepository implements IAccountRepository {
    
    private final Map<String, Account> accountsById = new ConcurrentHashMap<>();
    private final Map<String, Account> accountsByUsername = new ConcurrentHashMap<>();
    
    @Override
    public Account findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return accountsByUsername.get(username.toLowerCase());
    }
    
    @Override
    public Optional<Account> findById(String accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsById.get(accountId));
    }
    
    @Override
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (account.getAccountId() == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        if (account.getUsername() == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        
        // Create a defensive copy to avoid external modifications
        Account accountCopy = createCopy(account);
        
        accountsById.put(accountCopy.getAccountId(), accountCopy);
        accountsByUsername.put(accountCopy.getUsername().toLowerCase(), accountCopy);
        
        return createCopy(accountCopy);
    }
    
    @Override
    public void delete(Account account) {
        if (account != null && account.getAccountId() != null) {
            deleteById(account.getAccountId());
        }
    }
    
    @Override
    public void deleteById(String accountId) {
        if (accountId == null) {
            return;
        }
        
        Account account = accountsById.remove(accountId);
        if (account != null && account.getUsername() != null) {
            accountsByUsername.remove(account.getUsername().toLowerCase());
        }
    }
    
    @Override
    public List<Account> findAll() {
        return accountsById.values().stream()
                .map(this::createCopy)
                .toList();
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return username != null && accountsByUsername.containsKey(username.toLowerCase());
    }
    
    @Override
    public boolean existsById(String accountId) {
        return accountId != null && accountsById.containsKey(accountId);
    }
    
    @Override
    public long count() {
        return accountsById.size();
    }
    
    /**
     * Create a defensive copy of an account
     */
    private Account createCopy(Account original) {
        Account copy = new Account();
        copy.setAccountId(original.getAccountId());
        copy.setUsername(original.getUsername());
        copy.setBalance(original.getBalance());
        copy.setWatchlist(new ArrayList<>(original.getWatchlist()));
        copy.setAlerts(new ArrayList<>(original.getAlerts()));
        copy.setCreatedAt(original.getCreatedAt());
        copy.setLastLoginAt(original.getLastLoginAt());
        return copy;
    }
    
    /**
     * Clear all data (useful for testing)
     */
    public void clear() {
        accountsById.clear();
        accountsByUsername.clear();
    }
}
