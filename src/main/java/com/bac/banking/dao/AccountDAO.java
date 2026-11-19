package com.bac.banking.dao;

import com.bac.banking.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void createAccount(Account account) throws SQLException {
        String sql = """
            INSERT INTO accounts (account_number, customer_id, account_type, balance, branch, status,
                                  company_name, company_contact, company_address, company_turnover)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Enforce ACID transaction
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, account.getAccountNumber());
                pstmt.setString(2, account.getCustomer().getCustomerId());
                pstmt.setString(3, account.getAccountType());
                pstmt.setDouble(4, account.getBalance());
                pstmt.setString(5, account.getBranch());
                pstmt.setString(6, account.getStatus().name());

                if (account instanceof ChequeAccount chq) {
                    pstmt.setString(7, chq.getCompanyName());
                    pstmt.setString(8, chq.getCompanyContact());
                    pstmt.setString(9, chq.getCompanyAddress());
                    pstmt.setDouble(10, chq.getCompanyProfitTurnover());
                } else {
                    pstmt.setNull(7, Types.VARCHAR);
                    pstmt.setNull(8, Types.VARCHAR);
                    pstmt.setNull(9, Types.VARCHAR);
                    pstmt.setNull(10, Types.DOUBLE);
                }
                pstmt.executeUpdate();

                for (Transaction tx : account.getTransactionList()) {
                    transactionDAO.saveTransaction(conn, tx);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateBalanceAndLogTransaction(Account account) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // ACID compliance
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, account.getBalance());
                pstmt.setString(2, account.getAccountNumber());
                pstmt.executeUpdate();

                List<Transaction> txs = account.getTransactionList();
                if (!txs.isEmpty()) {
                    Transaction latest = txs.get(txs.size() - 1);
                    transactionDAO.saveTransaction(conn, latest);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void updateAccountStatus(String accountNumber, AccountStatus status) throws SQLException {
        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, accountNumber);
            pstmt.executeUpdate();
        }
    }

    public List<Account> findAccountsByCustomer(Customer customer) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustomerId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    accounts.add(hydrateAccount(rs, customer));
                }
            }
        }
        return accounts;
    }

    public List<Account> findAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        CustomerDAO customerDAO = new CustomerDAO();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Customer owner = customerDAO.findById(rs.getString("customer_id"));
                accounts.add(hydrateAccount(rs, owner));
            }
        }
        return accounts;
    }

    private Account hydrateAccount(ResultSet rs, Customer owner) throws SQLException {
        String accNum = rs.getString("account_number");
        String type = rs.getString("account_type");
        double bal = rs.getDouble("balance");
        String branch = rs.getString("branch");
        AccountStatus status = AccountStatus.valueOf(rs.getString("status"));

        Account account = switch (type) {
            case "SAVINGS" -> new SavingsAccount(accNum, bal, branch, owner);
            case "INVESTMENT" -> new InvestmentAccount(accNum, Math.max(bal, 500.0), branch, owner);
            case "CHEQUE" -> new ChequeAccount(
                    accNum, bal, branch, owner,
                    rs.getString("company_name"),
                    rs.getString("company_contact"),
                    rs.getString("company_address"),
                    rs.getDouble("company_turnover")
            );
            default -> throw new IllegalStateException("Unknown account type: " + type);
        };

        account.setBalance(bal);
        account.setStatus(status);
        account.setTransactionList(transactionDAO.findByAccountNumber(accNum));
        return account;
    }
}