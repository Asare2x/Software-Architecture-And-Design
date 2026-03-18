package com.shareanalysis.repository;

import com.shareanalysis.model.Account;

import java.util.*;

/**
 * In-memory implementation of AccountRepository.
 * Component: AccountRepository — stores account and wallet data.
 */
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> store = new LinkedHashMap<>();

    @Override
    public void save(Account account) {
        store.put(account.getAccountId(), account);
        System.out.println("[AccountRepository] Saved: " + account);
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(store.get(accountId));
    }

    @Override
    public void delete(String accountId) {
        store.remove(accountId);
        System.out.println("[AccountRepository] Deleted account: " + accountId);
    }
}
