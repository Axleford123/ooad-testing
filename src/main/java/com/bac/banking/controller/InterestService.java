package com.bac.banking.controller;

import com.bac.banking.dao.AccountDAO;
import com.bac.banking.model.Account;
import com.bac.banking.model.InterestBearing;

import java.sql.SQLException;
import java.util.List;

public class InterestService {
    private final AccountDAO accountDAO = new AccountDAO();

    /**
     * Executes the PayInterest State Machine across all bank accounts:
     * - SavingsAccount: credits 0.05% monthly
     * - InvestmentAccount: credits 5.0% monthly
     * - ChequeAccount: skips interest payment
     */
    public int calculateAndCreditMonthlyInterest() throws SQLException {
        List<Account> allAccounts = accountDAO.findAllAccounts();
        int creditedCount = 0;

        for (Account account : allAccounts) {
            if (account instanceof InterestBearing interestAccount) {
                double prevBalance = account.getBalance();
                interestAccount.applyMonthlyInterest();
                if (account.getBalance() > prevBalance) {
                    accountDAO.updateBalanceAndLogTransaction(account);
                    creditedCount++;
                }
            }
            // ChequeAccount does not implement InterestBearing and is skipped per State Diagram
        }
        return creditedCount;
    }
}