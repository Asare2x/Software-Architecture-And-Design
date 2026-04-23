package com.example.trading.service;

/**
 * Login contract used by the UI layer (LoginController / ConsoleView).
 * A subset of IAuthService focused purely on session management.
 * Implemented by AuthenticationService.
 */
public interface ILogin {
    boolean authenticate(String username, String password);
    void    logout();
    boolean isLoggedIn();
    String  getCurrentUser();
}
