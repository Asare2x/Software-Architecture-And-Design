package com.shareanalysis.repository;

import com.shareanalysis.model.Account;

import java.util.Optional;

/**
 * Component: AccountRepository
 * Direction: Account and Wallet Service → AccountRepository (stores account data)
 *
 * Interface for persisting user account and wallet information.
 */
public interface AccountRepository {

    /** Save or update an account. */
    void save(Account account);

    /** Find an account by its ID. */
    Optional<Account> findById(String accountId);

    /** Remove an account by its ID. */
    void delete(String accountId);
}
