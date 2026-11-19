package com.bac.banking.model;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String customerId;
    private String username;
    private String password;
    private String role; // "CUSTOMER" or "STAFF"
    private String firstname;
    private String surname;
    private String contacts;
    private String address;
    private List<Account> accounts;

    public Customer(String customerId, String username, String password, String role,
                    String firstname, String surname, String contacts, String address) {
        this.customerId = customerId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstname = firstname;
        this.surname = surname;
        this.contacts = contacts;
        this.address = address;
        this.accounts = new ArrayList<>();
    }

    public void addAccount(Account account) {
        for (Account existing : accounts) {
            if (existing.getClass().equals(account.getClass())) {
                throw new IllegalStateException("Customer already holds an account of type: " + account.getAccountType());
            }
        }
        this.accounts.add(account);
    }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public String getCustomerId() { return customerId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFirstname() { return firstname; }
    public String getSurname() { return surname; }
    public String getContacts() { return contacts; }
    public String getAddress() { return address; }

    public String getFullname() {
        return firstname + " " + surname;
    }
}