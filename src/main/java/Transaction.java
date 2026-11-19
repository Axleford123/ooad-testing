package com.bac.banking.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private String transactionId;
    private String accountNumber;
    private String type; // DEPOSIT, WITHDRAWAL, INTEREST, FEE
    private double amount;
    private double charge;
    private double balanceAfter;
    private LocalDateTime timestamp;
    private String description;

    public Transaction(String transactionId, String accountNumber, String type,
                       double amount, double charge, double balanceAfter, String description) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.charge = charge;
        this.balanceAfter = balanceAfter;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    public Transaction(String transactionId, String accountNumber, String type,
                       double amount, double charge, double balanceAfter,
                       LocalDateTime timestamp, String description) {
        this(transactionId, accountNumber, type, amount, charge, balanceAfter, description);
        this.timestamp = timestamp;
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getCharge() { return charge; }
    public double getBalanceAfter() { return balanceAfter; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("[%s] %-10s | Amt: BWP %.2f | Fee: BWP %.2f | Bal: BWP %.2f | %s",
                timestamp.format(fmt), type, amount, charge, balanceAfter, description);
    }
}