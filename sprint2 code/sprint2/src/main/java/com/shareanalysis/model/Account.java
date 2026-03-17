package com.shareanalysis.model;

/**
 * Represents a user account.
 * Component: Account and Wallet Service — stores account data via AccountRepository.
 */
public class Account {

    private final String accountId;
    private final String username;
    private       double walletBalance;

    public Account(String accountId, String username, double walletBalance) {
        this.accountId     = accountId;
        this.username      = username;
        this.walletBalance = walletBalance;
    }

    public String getAccountId()      { return accountId;     }
    public String getUsername()       { return username;      }
    public double getWalletBalance()  { return walletBalance; }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        this.walletBalance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > walletBalance) throw new IllegalArgumentException("Insufficient funds.");
        this.walletBalance -= amount;
    }

    @Override
    public String toString() {
        return String.format("Account{id='%s', user='%s', balance=%.2f}",
                accountId, username, walletBalance);
    }
}
