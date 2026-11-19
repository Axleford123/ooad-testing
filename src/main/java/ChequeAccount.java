package com.bac.banking.model;

public class ChequeAccount extends Account {
    private String companyName;
    private String companyContact;
    private String companyAddress;
    private double companyProfitTurnover;

    public ChequeAccount(String accountNumber, double initialBalance, String branch, Customer customer,
                         String companyName, String companyContact, String companyAddress, double companyProfitTurnover) {
        super(accountNumber, initialBalance, branch, customer);
        if (companyName == null || companyName.trim().isEmpty() ||
            companyAddress == null || companyAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Cheque Account requires valid company name and company address.");
        }
        this.companyName = companyName.trim();
        this.companyContact = companyContact;
        this.companyAddress = companyAddress.trim();
        this.companyProfitTurnover = companyProfitTurnover;

        if (initialBalance > 0) {
            recordTransaction("OPENING", initialBalance, 0.0, "Cheque Account Opening Deposit");
        }
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
        recordTransaction("WITHDRAWAL", amount, charge, "Cheque Account Withdrawal");
    }

    @Override
    public String getAccountType() {
        return "CHEQUE";
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyContact() { return companyContact; }
    public String getCompanyAddress() { return companyAddress; }
    public double getCompanyProfitTurnover() { return companyProfitTurnover; }
}