package com.bac.banking.model;

public class InvestmentAccount extends Account implements InterestBearing {
    public static final double MINIMUM_OPENING_DEPOSIT = 500.00;
    public static final double MONTHLY_INTEREST_RATE = 0.05; // 5% monthly

    public InvestmentAccount(String accountNumber, double initialDeposit, String branch, Customer customer) {
        super(accountNumber, initialDeposit, branch, customer);
        if (initialDeposit < MINIMUM_OPENING_DEPOSIT) {
            throw new IllegalArgumentException("Investment Account requires a minimum opening deposit of BWP 500.00.");
        }
        recordTransaction("OPENING", initialDeposit, 0.0, "Investment Account Opening Deposit");
    }

    @Override
    public void withdraw(double amount) {
        validateActiveStatus();
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than BWP 0.00.");
        }
        double charge = roundMoney(amount * WITHDRAWAL_FEE_RATE);
        double totalDeduction = roundMoney(amount + charge);

        if (totalDeduction > this.balance) {
            throw new IllegalArgumentException(
                String.format("Insufficient funds. Required: BWP %.2f (incl. BWP %.2f fee), Available: BWP %.2f",
                        totalDeduction, charge, this.balance));
        }

        this.balance = roundMoney(this.balance - totalDeduction);
        recordTransaction("WITHDRAWAL", amount, charge, "Investment Account Withdrawal");
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
            recordTransaction("INTEREST", interest, 0.0, "Monthly Investment Interest (5.0%)");
        }
    }

    @Override
    public String getAccountType() {
        return "INVESTMENT";
    }
}