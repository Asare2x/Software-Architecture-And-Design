package com.shareanalysis.service;

import com.shareanalysis.model.Account;
import com.shareanalysis.repository.AccountRepository;

import java.util.Optional;

/**
 * Component: Account and Wallet Service
 * Directions:
 *   Account and Wallet Service ← API              (exposes account endpoints)
 *   Account and Wallet Service → AccountRepository (stores account data)
 *
 * Manages user account creation, retrieval, and wallet transactions.
 * Exposes account operations through the API component.
 */
public class AccountWalletService {

    private final AccountRepository accountRepository;

    public AccountWalletService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /** Create and persist a new user account. */
    public Account createAccount(String accountId, String username, double initialBalance) {
        Account account = new Account(accountId, username, initialBalance);
        accountRepository.save(account);
        System.out.println("[AccountWalletService] Created: " + account);
        return account;
    }

    /** Retrieve an account by ID. */
    public Optional<Account> getAccount(String accountId) {
        return accountRepository.findById(accountId);
    }

    /** Deposit funds into a user's wallet. */
    public void deposit(String accountId, double amount) {
        accountRepository.findById(accountId).ifPresent(account -> {
            account.deposit(amount);
            accountRepository.save(account);
            System.out.printf("[AccountWalletService] Deposited %.2f to %s. New balance: %.2f%n",
                    amount, accountId, account.getWalletBalance());
        });
    }

    /** Withdraw funds from a user's wallet. */
    public void withdraw(String accountId, double amount) {
        accountRepository.findById(accountId).ifPresent(account -> {
            account.withdraw(amount);
            accountRepository.save(account);
            System.out.printf("[AccountWalletService] Withdrew %.2f from %s. New balance: %.2f%n",
                    amount, accountId, account.getWalletBalance());
        });
    }
}
