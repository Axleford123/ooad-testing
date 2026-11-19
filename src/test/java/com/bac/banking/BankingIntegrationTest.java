package com.bac.banking;

import java.util.List;
import com.bac.banking.controller.AccountController;
import com.bac.banking.controller.LoginController;
import com.bac.banking.dao.DatabaseManager;
import com.bac.banking.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BankingIntegrationTest {

    @BeforeAll
    static void setupDatabase() {
        DatabaseManager.setJdbcUrl("jdbc:h2:mem:test_banking_db;DB_CLOSE_DELAY=-1");
        DatabaseManager.initializeDatabase();
    }

    @Test
    void testFullCustomerBankingLifecycle() throws Exception {
        LoginController loginController = new LoginController();
        AccountController accountController = new AccountController();

        // 1. Register & Authenticate Customer
        Customer cust = loginController.registerCustomer(
                "testuser", "secret123", "Kagiso", "Motsumi", "71234567", "Block 3, Gaborone");
        Customer loggedIn = loginController.login("testuser", "secret123");
        assertEquals("Kagiso Motsumi", loggedIn.getFullname());

        // 2. Enforce Investment Account Minimum Deposit (BWP 500.00)
        assertThrows(IllegalArgumentException.class, () ->
                accountController.openInvestmentAccount(loggedIn, 200.00, "Main Mall"));

        Account inv = accountController.openInvestmentAccount(loggedIn, 1000.00, "Main Mall");
        assertEquals(1000.00, inv.getBalance());

        // 3. Enforce Cheque Account Employment Validation
        assertThrows(IllegalArgumentException.class, () ->
                accountController.openChequeAccount(loggedIn, 500.00, "CBD", "", "", "", 0));

        Account chq = accountController.openChequeAccount(
                loggedIn, 2000.00, "CBD", "Debswana", "3614200", "Gaborone", 1000000);

        // 4. Test Deposit with 2% charge (Deposit BWP 100 -> BWP 2 fee -> +BWP 98)
        double newChqBal = accountController.deposit(chq, 100.00);
        assertEquals(2098.00, newChqBal);

        // 5. Test Withdrawal with 1% charge (Withdraw BWP 100 -> BWP 1 fee -> -BWP 101)
        double afterWithdraw = accountController.withdraw(chq, 100.00);
        assertEquals(1997.00, afterWithdraw);

        // 6. Test Savings Account No-Withdrawal Rule & 0.05% Monthly Interest
        Account sav = accountController.openSavingsAccount(loggedIn, 10000.00, "Main Mall");
        assertThrows(UnsupportedOperationException.class, () -> accountController.withdraw(sav, 50.00));

        // 7. Run Monthly Interest Cycle (5% on Investment = +50, 0.05% on Savings = +5, Cheque skipped)
        accountController.runMonthlyInterestCycle();

List<Account> refreshedAccounts = accountController.refreshCustomerAccounts(loggedIn);

for (Account acc : refreshedAccounts) {
        
            if (acc instanceof InvestmentAccount) assertEquals(1050.00, acc.getBalance());
            if (acc instanceof SavingsAccount) assertEquals(10005.00, acc.getBalance());
            if (acc instanceof ChequeAccount) assertEquals(1997.00, acc.getBalance());
        }
    }
}