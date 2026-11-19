package com.bac.banking.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Account {
    public static final double DEPOSIT_FEE_RATE = 0.02;    // 2:100 charge rate per mock interview
    public static final double WITHDRAWAL_FEE_RATE = 0.01; // 1:100 charge rate per mock interview

    protected String accountNumber;
    protected double balance;
    protected String branch;
    protected Customer customer;
    protected AccountStatus status;
    protected List<Transaction> history;

    public Account(String accountNumber, double initialBalance, String branch, Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("An account cannot exist without a customer.");
        }
        this.accountNumber = accountNumber;
        this.balance = roundMoney(initialBalance);
        this.branch = branch;
        this.customer = customer;
        this.status = AccountStatus.ACTIVE;
        this.history = new ArrayList<>();
    }

    // Standard deposit with default description
    public void deposit(double amount) {
        deposit(amount, "Cash/Electronic Deposit");
    }

    // Overloaded deposit method (Method Overloading)
    public void deposit(double amount, String description) {
        validateActiveStatus();
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than BWP 0.00.");
        }
        double charge = roundMoney(amount * DEPOSIT_FEE_RATE);
        double netCredit = roundMoney(amount - charge);
        this.balance = roundMoney(this.balance + netCredit);

        recordTransaction("DEPOSIT", amount, charge, description);
    }

    // Abstract polymorphic withdrawal method (Method Overriding in subclasses)
    public abstract void withdraw(double amount);

    public abstract String getAccountType();

    protected void validateActiveStatus() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Operation denied: Account is currently " + this.status);
        }
    }

    protected void recordTransaction(String type, double amount, double charge, String description) {
        String txId = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Transaction tx = new Transaction(txId, this.accountNumber, type, amount, charge, this.balance, description);
        this.history.add(tx);
    }

    protected double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = roundMoney(balance); }
    public String getBranch() { return branch; }
    public Customer getCustomer() { return customer; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }
    public List<Transaction> getTransactionList() { return history; }
    public void setTransactionList(List<Transaction> history) { this.history = history; }

    public String getHistory() {
        if (history.isEmpty()) return "No transactions recorded.";
        StringBuilder sb = new StringBuilder();
        for (Transaction t : history) {
            sb.append(t.toString()).append("\n");
        }
        return sb.toString();
    }
}