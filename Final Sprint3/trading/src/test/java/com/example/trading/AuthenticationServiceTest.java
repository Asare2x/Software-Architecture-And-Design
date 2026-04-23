package com.example.trading;

import com.example.trading.service.AuthenticationService;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for AuthenticationService.
 *
 * Tests cover:
 *  TC-AUTH-01  Valid login with default credentials
 *  TC-AUTH-02  Invalid password rejected
 *  TC-AUTH-03  Non-existent user rejected
 *  TC-AUTH-04  Null inputs rejected gracefully
 *  TC-AUTH-05  Session state after login
 *  TC-AUTH-06  Session cleared after logout
 *  TC-AUTH-07  Register new user and login
 *  TC-AUTH-08  Duplicate username registration rejected
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationServiceTest {

    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        // Use a fresh instance each test to avoid state bleed
        authService = AuthenticationService.getInstance();
        authService.logout(); // ensure clean state
    }

    // TC-AUTH-01
    @Test @Order(1)
    @DisplayName("TC-AUTH-01: Valid credentials authenticate successfully")
    void testValidLogin() {
        boolean result = authService.authenticate("admin", "admin123");
        assertTrue(result, "Valid credentials should authenticate");
    }

    // TC-AUTH-02
    @Test @Order(2)
    @DisplayName("TC-AUTH-02: Wrong password is rejected")
    void testInvalidPassword() {
        boolean result = authService.authenticate("admin", "wrongpassword");
        assertFalse(result, "Wrong password should not authenticate");
    }

    // TC-AUTH-03
    @Test @Order(3)
    @DisplayName("TC-AUTH-03: Non-existent user is rejected")
    void testNonExistentUser() {
        boolean result = authService.authenticate("nobody", "password");
        assertFalse(result, "Non-existent user should not authenticate");
    }

    // TC-AUTH-04
    @Test @Order(4)
    @DisplayName("TC-AUTH-04: Null inputs handled gracefully")
    void testNullInputs() {
        assertFalse(authService.authenticate(null, null));
        assertFalse(authService.authenticate("admin", null));
        assertFalse(authService.authenticate(null, "admin123"));
    }

    // TC-AUTH-05
    @Test @Order(5)
    @DisplayName("TC-AUTH-05: Session state is set after login")
    void testSessionAfterLogin() {
        authService.authenticate("admin", "admin123");
        assertTrue(authService.isLoggedIn());
        assertEquals("admin", authService.getCurrentUser());
    }

    // TC-AUTH-06
    @Test @Order(6)
    @DisplayName("TC-AUTH-06: Session is cleared after logout")
    void testSessionAfterLogout() {
        authService.authenticate("admin", "admin123");
        authService.logout();
        assertFalse(authService.isLoggedIn());
        assertNull(authService.getCurrentUser());
    }

    // TC-AUTH-07
    @Test @Order(7)
    @DisplayName("TC-AUTH-07: New user can register and then login")
    void testRegisterAndLogin() {
        String username = "testuser_" + System.currentTimeMillis();
        boolean registered = authService.register(username, "testpass123");
        assertTrue(registered, "Registration should succeed for new username");
        boolean loggedIn = authService.authenticate(username, "testpass123");
        assertTrue(loggedIn, "Newly registered user should be able to login");
    }

    // TC-AUTH-08
    @Test @Order(8)
    @DisplayName("TC-AUTH-08: Duplicate username registration is rejected")
    void testDuplicateRegistration() {
        boolean first  = authService.register("uniqueuser99", "pass123");
        boolean second = authService.register("uniqueuser99", "pass456");
        assertTrue(first,   "First registration should succeed");
        assertFalse(second, "Duplicate registration should fail");
    }
}
