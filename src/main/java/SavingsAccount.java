package com.bac.banking.model;

public class SavingsAccount extends Account implements InterestBearing {
    public static final double MONTHLY_INTEREST_RATE = 0.0005; // 0.05% monthly

    public SavingsAccount(String accountNumber, double initialBalance, String branch, Customer customer) {
        super(accountNumber, initialBalance, branch, customer);
        if (initialBalance > 0) {
            recordTransaction("OPENING", initialBalance, 0.0, "Savings Account Opening Deposit");
        }
    }

    @Override
    public void withdraw(double amount) {
        throw new UnsupportedOperationException("Withdrawals are strictly prohibited on a Savings Account.");
    }

    @Override
    public double calculateMonthlyInterest() {
        return roundMoney(this.balance * MONTHLY_INTEREST_RATE);
    }

    @Override
    public void applyMonthlyInterest() {
        if (this.status != AccountStatus.ACTIVE) return;
        double interest = calculateMonthlyInterest();
        if (interest > 0) {
            this.balance = roundMoney(this.balance + interest);
            recordTransaction("INTEREST", interest, 0.0, "Monthly Savings Interest (0.05%)");
        }
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }
}