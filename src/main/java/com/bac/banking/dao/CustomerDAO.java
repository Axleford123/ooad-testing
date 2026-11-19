package com.bac.banking.dao;

import com.bac.banking.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public void saveCustomer(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (customer_id, username, password, role, firstname, surname, contacts, address)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getCustomerId());
            pstmt.setString(2, customer.getUsername());
            pstmt.setString(3, customer.getPassword());
            pstmt.setString(4, customer.getRole());
            pstmt.setString(5, customer.getFirstname());
            pstmt.setString(6, customer.getSurname());
            pstmt.setString(7, customer.getContacts());
            pstmt.setString(8, customer.getAddress());
            pstmt.executeUpdate();
        }
    }

    public Customer findCustomer(String username) throws SQLException {
        String sql = "SELECT * FROM customers WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> findAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE role = 'CUSTOMER'";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("contacts"),
                rs.getString("address")
        );
    }
}