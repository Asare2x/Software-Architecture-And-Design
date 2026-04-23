package com.example.trading;

import com.example.trading.exception.ServiceException;
import com.example.trading.repository.InMemoryAccountRepository;
import com.example.trading.service.AccountWalletService;
import com.example.trading.service.AuthenticationService;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for AccountWalletService.
 *
 * Tests cover:
 *  TC-WAL-01  Account created automatically on first access
 *  TC-WAL-02  Deposit increases balance correctly
 *  TC-WAL-03  Withdraw decreases balance correctly
 *  TC-WAL-04  Withdraw with insufficient funds throws ServiceException
 *  TC-WAL-05  Zero/negative deposit rejected
 *  TC-WAL-06  Transfer between accounts works
 *  TC-WAL-07  Transfer to non-existent account throws ServiceException
 *  TC-WAL-08  Watchlist add and remove
 *  TC-WAL-09  Duplicate watchlist entries are ignored
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountWalletServiceTest {

    private AccountWalletService walletService;
    private InMemoryAccountRepository repo;

    @BeforeEach
    void setUp() {
        AuthenticationService.getInstance().authenticate("admin", "admin123");
        repo = new InMemoryAccountRepository();
        walletService = new AccountWalletService(repo, AuthenticationService.getInstance());
    }

    @AfterEach
    void tearDown() { AuthenticationService.getInstance().logout(); }

    // TC-WAL-01
    @Test @Order(1)
    @DisplayName("TC-WAL-01: Account is created automatically on first access")
    void testAccountAutoCreated() {
        com.example.trading.model.Account account = walletService.getCurrentUserAccount();
        assertNotNull(account);
        assertEquals("admin", account.getUsername());
        assertNotNull(account.getAccountId());
    }

    // TC-WAL-02
    @Test @Order(2)
    @DisplayName("TC-WAL-02: Deposit increases balance")
    void testDeposit() {
        walletService.deposit(new BigDecimal("500.00"));
        assertEquals(0, new BigDecimal("500.00").compareTo(walletService.getBalance()));
    }

    // TC-WAL-03
    @Test @Order(3)
    @DisplayName("TC-WAL-03: Withdraw decreases balance")
    void testWithdraw() {
        walletService.deposit(new BigDecimal("1000.00"));
        walletService.withdraw(new BigDecimal("300.00"));
        assertEquals(0, new BigDecimal("700.00").compareTo(walletService.getBalance()));
    }

    // TC-WAL-04
    @Test @Order(4)
    @DisplayName("TC-WAL-04: Withdraw with insufficient funds throws ServiceException")
    void testWithdrawInsufficientFunds() {
        walletService.deposit(new BigDecimal("100.00"));
        assertThrows(ServiceException.class,
                () -> walletService.withdraw(new BigDecimal("500.00")));
    }

    // TC-WAL-05
    @Test @Order(5)
    @DisplayName("TC-WAL-05: Zero and negative deposits are rejected")
    void testInvalidDeposit() {
        assertThrows(ServiceException.class, () -> walletService.deposit(BigDecimal.ZERO));
        assertThrows(ServiceException.class, () -> walletService.deposit(new BigDecimal("-100")));
    }

    // TC-WAL-06
    @Test @Order(6)
    @DisplayName("TC-WAL-06: Transfer between two accounts works correctly")
    void testTransfer() {
        // Create a recipient account
        AuthenticationService.getInstance().register("recipient_test", "pass");
        com.example.trading.model.Account recipient = new com.example.trading.model.Account();
        recipient.setAccountId("ACC-recipient_test");
        recipient.setUsername("recipient_test");
        recipient.setBalance(BigDecimal.ZERO);
        repo.save(recipient);

        walletService.deposit(new BigDecimal("1000.00"));
        walletService.transfer("recipient_test", new BigDecimal("250.00"));

        assertEquals(0, new BigDecimal("750.00").compareTo(walletService.getBalance()),
                "Sender balance should decrease by transfer amount");
        com.example.trading.model.Account updated = repo.findByUsername("recipient_test");
        assertEquals(0, new BigDecimal("250.00").compareTo(updated.getBalance()),
                "Recipient balance should increase by transfer amount");
    }

    // TC-WAL-07
    @Test @Order(7)
    @DisplayName("TC-WAL-07: Transfer to non-existent account throws ServiceException")
    void testTransferToNonExistentAccount() {
        walletService.deposit(new BigDecimal("500.00"));
        assertThrows(ServiceException.class,
                () -> walletService.transfer("ghost_user_xyz", new BigDecimal("100.00")));
    }

    // TC-WAL-08
    @Test @Order(8)
    @DisplayName("TC-WAL-08: Watchlist add and remove work correctly")
    void testWatchlist() {
        walletService.addToWatchlist("AAPL");
        walletService.addToWatchlist("MSFT");
        assertTrue(walletService.getWatchlist().contains("AAPL"));
        assertTrue(walletService.getWatchlist().contains("MSFT"));

        walletService.removeFromWatchlist("AAPL");
        assertFalse(walletService.getWatchlist().contains("AAPL"));
        assertTrue(walletService.getWatchlist().contains("MSFT"));
    }

    // TC-WAL-09
    @Test @Order(9)
    @DisplayName("TC-WAL-09: Duplicate watchlist entries are ignored")
    void testWatchlistNoDuplicates() {
        walletService.addToWatchlist("TSLA");
        walletService.addToWatchlist("TSLA");
        long count = walletService.getWatchlist().stream()
                .filter(s -> s.equals("TSLA")).count();
        assertEquals(1, count, "TSLA should appear only once in watchlist");
    }
}
