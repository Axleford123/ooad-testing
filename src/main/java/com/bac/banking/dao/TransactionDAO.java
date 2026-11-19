package com.bac.banking.dao;

import com.bac.banking.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void saveTransaction(Connection conn, Transaction tx) throws SQLException {
        String sql = """
            INSERT INTO transactions (transaction_id, account_number, type, amount, charge, balance_after, timestamp, description)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tx.getTransactionId());
            pstmt.setString(2, tx.getAccountNumber());
            pstmt.setString(3, tx.getType());
            pstmt.setDouble(4, tx.getAmount());
            pstmt.setDouble(5, tx.getCharge());
            pstmt.setDouble(6, tx.getBalanceAfter());
            pstmt.setTimestamp(7, Timestamp.valueOf(tx.getTimestamp()));
            pstmt.setString(8, tx.getDescription());
            pstmt.executeUpdate();
        }
    }

    public List<Transaction> findByAccountNumber(String accountNumber) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("transaction_id"),
                            rs.getString("account_number"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getDouble("charge"),
                            rs.getDouble("balance_after"),
                            rs.getTimestamp("timestamp").toLocalDateTime(),
                            rs.getString("description")
                    ));
                }
            }
        }
        return list;
    }
}