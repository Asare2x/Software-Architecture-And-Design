package com.example.trading.repository;

/**
 * Generic database component contract — used by AccountWalletService.
 * Swap the implementation (in-memory, SQLite, PostgreSQL, etc.)
 * without changing any other class.
 */
public interface IDatabase {
    void    connect();
    void    disconnect();
    boolean isConnected();
    void    save(String collection, String key, Object value);
    Object  load(String collection, String key);
    void    delete(String collection, String key);
    boolean exists(String collection, String key);
}
