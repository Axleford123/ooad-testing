package com.bac.banking.controller;

import com.bac.banking.dao.AccountDAO;
import com.bac.banking.dao.TransactionDAO;
import com.bac.banking.model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class AccountController {
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final InterestService interestService = new InterestService();

    public Account openSavingsAccount(Customer customer, double initialDeposit, String branch) throws SQLException {
        String accNum = "SAV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        SavingsAccount account = new SavingsAccount(accNum, initialDeposit, branch, customer);
        customer.addAccount(account);
        accountDAO.createAccount(account);
        return account;
    }

    public Account openInvestmentAccount(Customer customer, double initialDeposit, String branch) throws SQLException {
        String accNum = "INV-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        InvestmentAccount account = new InvestmentAccount(accNum, initialDeposit, branch, customer);
        customer.addAccount(account);
        accountDAO.createAccount(account);
        return account;
    }

    public Account openChequeAccount(Customer customer, double initialDeposit, String branch,
                                     String companyName, String companyContact,
                                     String companyAddress, double turnover) throws SQLException {
        String accNum = "CHQ-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        ChequeAccount account = new ChequeAccount(accNum, initialDeposit, branch, customer,
                companyName, companyContact, companyAddress, turnover);
        customer.addAccount(account);
        accountDAO.createAccount(account);
        return account;
    }

    public double deposit(Account account, double amount) throws SQLException {
        account.deposit(amount);
        accountDAO.updateBalanceAndLogTransaction(account);
        return account.getBalance();
    }

    public double withdraw(Account account, double amount) throws SQLException {
        account.withdraw(amount);
        accountDAO.updateBalanceAndLogTransaction(account);
        return account.getBalance();
    }

    public List<Account> refreshCustomerAccounts(Customer customer) throws SQLException {
        List<Account> updated = accountDAO.findAccountsByCustomer(customer);
        customer.setAccounts(updated);
        return updated;
    }

    public List<Account> getAllSystemAccounts() throws SQLException {
        return accountDAO.findAllAccounts();
    }

    public void updateAccountStatusByStaff(Customer actor, String accountNumber, AccountStatus newStatus) throws SQLException {
        if (!"STAFF".equalsIgnoreCase(actor.getRole())) {
            throw new SecurityException("Only authorized Bank Staff can change account statuses.");
        }
        accountDAO.updateAccountStatus(accountNumber, newStatus);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) throws SQLException {
        return transactionDAO.findByAccountNumber(accountNumber);
    }

    public int runMonthlyInterestCycle() throws SQLException {
        return interestService.calculateAndCreditMonthlyInterest();
    }
}