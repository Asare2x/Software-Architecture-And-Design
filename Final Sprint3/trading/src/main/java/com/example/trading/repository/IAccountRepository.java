package com.example.trading.repository;

import com.example.trading.model.Account;
import java.util.List;
import java.util.Optional;

/**
 * Account repository contract — used by AccountWalletService.
 * Implemented by InMemoryAccountRepository.
 * Swap for a database-backed implementation without changing service code.
 */
public interface IAccountRepository {
    Account           save(Account account);
    Account           findByUsername(String username);
    Optional<Account> findById(String accountId);
    List<Account>     findAll();
    void              delete(Account account);
    void              deleteById(String accountId);
    boolean           existsByUsername(String username);
    boolean           existsById(String accountId);
    long              count();
}
