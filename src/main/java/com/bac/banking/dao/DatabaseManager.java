package com.bac.banking.dao;

import com.bac.banking.model.*;

import java.sql.*;

public class DatabaseManager {
    private static String jdbcUrl = "jdbc:h2:./data/bac_banking_db;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void setJdbcUrl(String url) {
        jdbcUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, USER, PASS);
    }

    public static void initializeDatabase() {
        String createCustomers = """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id VARCHAR(36) PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL,
                role VARCHAR(20) NOT NULL,
                firstname VARCHAR(100) NOT NULL,
                surname VARCHAR(100) NOT NULL,
                contacts VARCHAR(50),
                address VARCHAR(255) NOT NULL
            )
        """;

        String createAccounts = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number VARCHAR(36) PRIMARY KEY,
                customer_id VARCHAR(36) NOT NULL,
                account_type VARCHAR(20) NOT NULL,
                balance DOUBLE NOT NULL,
                branch VARCHAR(100) NOT NULL,
                status VARCHAR(20) NOT NULL,
                company_name VARCHAR(150),
                company_contact VARCHAR(50),
                company_address VARCHAR(255),
                company_turnover DOUBLE,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )
        """;

        String createTransactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id VARCHAR(36) PRIMARY KEY,
                account_number VARCHAR(36) NOT NULL,
                type VARCHAR(20) NOT NULL,
                amount DOUBLE NOT NULL,
                charge DOUBLE NOT NULL,
                balance_after DOUBLE NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                description VARCHAR(255),
                FOREIGN KEY (account_number) REFERENCES accounts(account_number)
            )
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createCustomers);
            stmt.execute(createAccounts);
            stmt.execute(createTransactions);
            seedSampleRecords(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema: " + e.getMessage(), e);
        }
    }

    // Inserts 10 sample customer records (plus 1 bank staff admin) per Assignment Guideline #4
    private static void seedSampleRecords(Connection conn) throws SQLException {
        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM customers")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already seeded
            }
        }

        CustomerDAO customerDAO = new CustomerDAO();
        AccountDAO accountDAO = new AccountDAO();

        // Bank Staff user for managing account statuses (Active, Frozen, Closed)
        Customer staff = new Customer("STAFF-001", "admin", "admin123", "STAFF",
                "Boitumelo", "Kgosidintsi", "71000000", "Plot 54355, CBD Gaborone");
        customerDAO.saveCustomer(staff);

        // 10 Sample Customers
        Object[][] sampleData = {
            {"CUST-001", "lesley", "pass123", "Lesley", "Rankata", "72111001", "Block 6, Gaborone", "Debswana", "3614200", "Orapa House, Gaborone", 1500000.0},
            {"CUST-002", "thabo", "pass123", "Thabo", "Mokoena", "72111002", "Phase 2, Gaborone", "Mascom Wireless", "3903396", "Tsholetsa House, Gaborone", 950000.0},
            {"CUST-003", "kabo", "pass123", "Kabo", "Modise", "72111003", "Extension 9, Gaborone", "BTC", "3958000", "Megaleng, Gaborone", 820000.0},
            {"CUST-004", "neo", "pass123", "Neo", "Molefe", "72111004", "Phakalane, Gaborone", "FNBB", "3706000", "First Place, CBD", 2400000.0},
            {"CUST-005", "lorato", "pass123", "Lorato", "Sebego", "72111005", "Tlokweng", "Water Utilities", "3604400", "Sedibeng House, Gaborone", 640000.0},
            {"CUST-006", "tebogo", "pass123", "Tebogo", "Ndlovu", "72111006", "Mogoditshane", "Choppies", "3186657", "Airport Junction, Gaborone", 1100000.0},
            {"CUST-007", "mpho", "pass123", "Mpho", "Khumalo", "72111007", "Broadhurst, Gaborone", "BPC", "3603000", "Motlakase House, Gaborone", 780000.0},
            {"CUST-008", "ame", "pass123", "Ame", "Tau", "72111008", "Village, Gaborone", "BAC", "3953062", "Fairgrounds, Gaborone", 520000.0},
            {"CUST-009", "katlego", "pass123", "Katlego", "Phiri", "72111009", "Block 8, Gaborone", "STANBIC", "3618000", "Fairground Office Park", 1900000.0},
            {"CUST-010", "onkarabile", "pass123", "Onkarabile", "Sefako", "72111010", "Gaborone West", "Sefalana", "3913661", "Plot 10242, Gaborone", 890000.0}
        };

        for (int i = 0; i < sampleData.length; i++) {
            Object[] row = sampleData[i];
            Customer c = new Customer((String) row[0], (String) row[1], (String) row[2], "CUSTOMER",
                    (String) row[3], (String) row[4], (String) row[5], (String) row[6]);
            customerDAO.saveCustomer(c);

            // Give each sample customer a Savings Account and an Investment Account
            SavingsAccount sav = new SavingsAccount("SAV-100" + (i + 1), 1500.0 + (i * 200), "Main Mall Branch", c);
            accountDAO.createAccount(sav);

            InvestmentAccount inv = new InvestmentAccount("INV-200" + (i + 1), 1000.0 + (i * 500), "CBD Branch", c);
            accountDAO.createAccount(inv);

            // Give the first 5 customers a Cheque Account as well
            if (i < 5) {
                ChequeAccount chq = new ChequeAccount("CHQ-300" + (i + 1), 3500.0 + (i * 300), "Fairgrounds Branch", c,
                        (String) row[7], (String) row[8], (String) row[9], (Double) row[10]);
                accountDAO.createAccount(chq);
            }
        }
    }
}