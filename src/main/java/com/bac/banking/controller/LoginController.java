package com.bac.banking.controller;

import com.bac.banking.dao.AccountDAO;
import com.bac.banking.dao.CustomerDAO;
import com.bac.banking.model.Customer;

import java.sql.SQLException;
import java.util.UUID;

public class LoginController {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public Customer login(String username, String password) throws Exception {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        Customer customer = customerDAO.findCustomer(username.trim());
        if (customer == null || !customer.getPassword().equals(password)) {
            throw new SecurityException("Invalid credentials.");
        }
        customer.setAccounts(accountDAO.findAccountsByCustomer(customer));
        return customer;
    }

    public Customer registerCustomer(String username, String password, String firstname,
                                     String surname, String contacts, String address) throws Exception {
        if (username.isBlank() || password.isBlank() || firstname.isBlank() ||
            surname.isBlank() || address.isBlank()) {
            throw new IllegalArgumentException("All mandatory customer registration fields must be filled.");
        }
        if (customerDAO.findCustomer(username.trim()) != null) {
            throw new IllegalArgumentException("Username already exists.");
        }
        String customerId = "CUST-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Customer newCustomer = new Customer(customerId, username.trim(), password, "CUSTOMER",
                firstname.trim(), surname.trim(), contacts.trim(), address.trim());
        customerDAO.saveCustomer(newCustomer);
        return newCustomer;
    }
}