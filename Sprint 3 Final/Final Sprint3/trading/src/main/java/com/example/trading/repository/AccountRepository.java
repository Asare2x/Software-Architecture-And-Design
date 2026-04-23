package com.example.trading.repository;

import com.example.trading.model.Account;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    /**
     * Find account by username
     * @param username the username to search for
     * @return Account if found, null otherwise
     */
    Account findByUsername(String username);
    
    /**
     * Find account by account ID
     * @param accountId the account ID to search for
     * @return Optional containing the account if found
     */
    Optional<Account> findById(String accountId);
    
    /**
     * Save or update an account
     * @param account the account to save
     * @return the saved account
     */
    Account save(Account account);
    
    /**
     * Delete an account
     * @param account the account to delete
     */
    void delete(Account account);
    
    /**
     * Delete account by ID
     * @param accountId the account ID to delete
     */
    void deleteById(String accountId);
    
    /**
     * Find all accounts
     * @return list of all accounts
     */
    List<Account> findAll();
    
    /**
     * Check if account exists by username
     * @param username the username to check
     * @return true if account exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if account exists by ID
     * @param accountId the account ID to check
     * @return true if account exists, false otherwise
     */
    boolean existsById(String accountId);
    
    /**
     * Count total number of accounts
     * @return total count of accounts
     */
    long count();
}
