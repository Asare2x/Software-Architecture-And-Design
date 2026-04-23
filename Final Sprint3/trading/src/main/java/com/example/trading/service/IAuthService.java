package com.example.trading.service;

/**
 * Authentication service contract used by SharePriceService to verify sessions,
 * and by the UI layer via ILogin.
 * Implemented by AuthenticationService.
 */
public interface IAuthService {
    boolean authenticate(String username, String password);
    void    logout();
    boolean isLoggedIn();
    String  getCurrentUser();
    boolean register(String username, String password);
    boolean userExists(String username);
}
