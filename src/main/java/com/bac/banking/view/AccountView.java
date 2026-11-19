package com.bac.banking.view;

import com.bac.banking.controller.AccountController;
import com.bac.banking.model.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class AccountView {
    private final Stage stage;
    private final Customer currentUser;
    private final AccountController accountController = new AccountController();

    private ComboBox<Account> accountSelector;
    private Label balanceLabel;
    private Label statusBadge;
    private TextArea historyArea;
    private Label feedbackLabel;

    public AccountView(Stage stage, Customer currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Top Header
        HBox header = new HBox(15);
        Label welcome = new Label("Welcome, " + currentUser.getFullname() + " (" + currentUser.getRole() + ")");
        welcome.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button interestBtn = new Button("Run Monthly Interest Cycle");
        interestBtn.setOnAction(e -> handleRunInterest());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> new LoginView(stage).show());

        header.getChildren().addAll(welcome, spacer, interestBtn, logoutBtn);
        root.setTop(header);

        // Center Content
        VBox centerBox = new VBox(12);
        centerBox.setPadding(new Insets(15, 0, 15, 0));

        accountSelector = new ComboBox<>();
        accountSelector.setPrefWidth(360);
        accountSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" :
                        item.getAccountNumber() + " - " + item.getAccountType() + " (" + item.getCustomer().getFullname() + ")");
            }
        });
        accountSelector.setButtonCell(accountSelector.getCellFactory().call(null));
        accountSelector.setOnAction(e -> updateAccountDetails());

        balanceLabel = new Label("Balance: BWP 0.00");
        balanceLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        statusBadge = new Label("Status: -");

        // Deposit / Withdraw Controls
        HBox txControls = new HBox(10);
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (BWP)");

        Button depositBtn = new Button("Deposit (2% Fee)");
        depositBtn.setOnAction(e -> handleDeposit(amountField.getText()));

        Button withdrawBtn = new Button("Withdraw (1% Fee)");
        withdrawBtn.setOnAction(e -> handleWithdraw(amountField.getText()));

        txControls.getChildren().addAll(amountField, depositBtn, withdrawBtn);

        // Staff-only Status Control
        HBox staffControls = new HBox(10);
        if ("STAFF".equalsIgnoreCase(currentUser.getRole())) {
            ComboBox<AccountStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(AccountStatus.values()));
            statusBox.setPromptText("Set Status");
            Button applyStatusBtn = new Button("Update Account Status");
            applyStatusBtn.setOnAction(e -> {
                Account selected = accountSelector.getValue();
                if (selected != null && statusBox.getValue() != null) {
                    try {
                        accountController.updateAccountStatusByStaff(currentUser, selected.getAccountNumber(), statusBox.getValue());
                        refreshAccounts();
                        setFeedback("Account status updated to " + statusBox.getValue(), false);
                    } catch (Exception ex) {
                        setFeedback(ex.getMessage(), true);
                    }
                }
            });
            staffControls.getChildren().addAll(new Label("Staff Controls:"), statusBox, applyStatusBtn);
        }

        // Open New Account Section (Customers)
        TitledPane openAccPane = buildOpenAccountPane();

        // Transaction History Area
        historyArea = new TextArea();
        historyArea.setEditable(false);
        historyArea.setPrefHeight(180);

        feedbackLabel = new Label();

        centerBox.getChildren().addAll(
                new Label("Select Account:"), accountSelector,
                balanceLabel, statusBadge,
                txControls, staffControls, openAccPane,
                new Label("Transaction History:"), historyArea, feedbackLabel
        );

        root.setCenter(centerBox);
        refreshAccounts();

        stage.setScene(new Scene(root, 720, 640));
        stage.show();
    }

    private TitledPane buildOpenAccountPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("SAVINGS", "INVESTMENT", "CHEQUE"));
        typeBox.setValue("SAVINGS");
        TextField initDepField = new TextField("500.00");
        TextField branchField = new TextField("Main Mall Branch");
        TextField compNameField = new TextField();
        compNameField.setPromptText("Company Name (Cheque only)");
        TextField compContactField = new TextField();
        compContactField.setPromptText("Company Contact");
        TextField compAddrField = new TextField();
        compAddrField.setPromptText("Company Address (Cheque only)");

        Button openBtn = new Button("Open Account");
        openBtn.setOnAction(e -> {
            try {
                double initAmt = Double.parseDouble(initDepField.getText());
                String branch = branchField.getText();
                switch (typeBox.getValue()) {
                    case "SAVINGS" -> accountController.openSavingsAccount(currentUser, initAmt, branch);
                    case "INVESTMENT" -> accountController.openInvestmentAccount(currentUser, initAmt, branch);
                    case "CHEQUE" -> accountController.openChequeAccount(currentUser, initAmt, branch,
                            compNameField.getText(), compContactField.getText(), compAddrField.getText(), 0.0);
                }
                refreshAccounts();
                setFeedback(typeBox.getValue() + " account opened successfully.", false);
            } catch (Exception ex) {
                setFeedback(ex.getMessage(), true);
            }
        });

        grid.addRow(0, new Label("Account Type:"), typeBox, new Label("Initial Deposit:"), initDepField);
        grid.addRow(1, new Label("Branch:"), branchField, new Label("Company Name:"), compNameField);
        grid.addRow(2, new Label("Company Contact:"), compContactField, new Label("Company Address:"), compAddrField);
        grid.add(openBtn, 0, 3);

        TitledPane pane = new TitledPane("Open New Bank Account", grid);
        pane.setExpanded(false);
        return pane;
    }

    private void handleDeposit(String rawAmount) {
        Account selected = accountSelector.getValue();
        if (selected == null) return;
        try {
            double amt = Double.parseDouble(rawAmount);
            double newBal = accountController.deposit(selected, amt);
            updateAccountDetails();
            setFeedback(String.format("Deposited BWP %.2f (2%% fee applied). New Balance: BWP %.2f", amt, newBal), false);
        } catch (Exception ex) {
            setFeedback(ex.getMessage(), true);
        }
    }

    private void handleWithdraw(String rawAmount) {
        Account selected = accountSelector.getValue();
        if (selected == null) return;
        try {
            double amt = Double.parseDouble(rawAmount);
            double newBal = accountController.withdraw(selected, amt);
            updateAccountDetails();
            setFeedback(String.format("Withdrew BWP %.2f (1%% fee applied). New Balance: BWP %.2f", amt, newBal), false);
        } catch (Exception ex) {
            setFeedback(ex.getMessage(), true);
        }
    }

    private void handleRunInterest() {
        try {
            int count = accountController.runMonthlyInterestCycle();
            refreshAccounts();
            setFeedback("Monthly interest cycle completed. Credited " + count + " eligible accounts.", false);
        } catch (Exception ex) {
            setFeedback(ex.getMessage(), true);
        }
    }

    private void refreshAccounts() {
        try {
            List<Account> list = "STAFF".equalsIgnoreCase(currentUser.getRole())
                    ? accountController.getAllSystemAccounts()
                    : accountController.refreshCustomerAccounts(currentUser);
            accountSelector.setItems(FXCollections.observableArrayList(list));
            if (!list.isEmpty()) {
                accountSelector.getSelectionModel().selectFirst();
                updateAccountDetails();
            }
        } catch (Exception ex) {
            setFeedback(ex.getMessage(), true);
        }
    }

    private void updateAccountDetails() {
        Account selected = accountSelector.getValue();
        if (selected == null) return;
        try {
            balanceLabel.setText(String.format("Balance: BWP %.2f", selected.getBalance()));
            statusBadge.setText("Status: " + selected.getStatus());
            List<Transaction> txs = accountController.getTransactionHistory(selected.getAccountNumber());
            StringBuilder sb = new StringBuilder();
            for (Transaction tx : txs) {
                sb.append(tx.toString()).append("\n");
            }
            historyArea.setText(sb.toString());
        } catch (Exception ex) {
            setFeedback(ex.getMessage(), true);
        }
    }

    private void setFeedback(String msg, boolean isError) {
        feedbackLabel.setStyle(isError ? "-fx-text-fill: red;" : "-fx-text-fill: green;");
        feedbackLabel.setText(msg);
    }
}