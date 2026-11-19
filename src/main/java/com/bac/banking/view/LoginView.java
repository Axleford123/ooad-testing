package com.bac.banking.view;

import com.bac.banking.controller.LoginController;
import com.bac.banking.model.Customer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class LoginView {
    private final Stage stage;
    private final LoginController loginController = new LoginController();

    public LoginView(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        TabPane tabPane = new TabPane();

        // --- TAB 1: LOGIN ---
        VBox loginBox = new VBox(12);
        loginBox.setPadding(new Insets(20));
        loginBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Botswana Banking System - Login");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setPromptText("Username (e.g. lesley or admin)");

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password (e.g. pass123 or admin123)");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");

        Button loginBtn = new Button("Login");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setOnAction(e -> {
            try {
                Customer customer = loginController.login(userField.getText(), passField.getText());
                new AccountView(stage, customer).show();
            } catch (Exception ex) {
                statusLabel.setText(ex.getMessage());
            }
        });

        loginBox.getChildren().addAll(title, new Label("Username:"), userField,
                new Label("Password:"), passField, loginBtn, statusLabel);

        Tab loginTab = new Tab("Login", loginBox);
        loginTab.setClosable(false);

        // --- TAB 2: REGISTER NEW CUSTOMER ---
        GridPane regGrid = new GridPane();
        regGrid.setPadding(new Insets(20));
        regGrid.setHgap(10);
        regGrid.setVgap(10);

        TextField regUser = new TextField();
        PasswordField regPass = new PasswordField();
        TextField regFirst = new TextField();
        TextField regSur = new TextField();
        TextField regContact = new TextField();
        TextField regAddr = new TextField();
        Label regMsg = new Label();

        regGrid.addRow(0, new Label("Username*:"), regUser);
        regGrid.addRow(1, new Label("Password*:"), regPass);
        regGrid.addRow(2, new Label("First Name*:"), regFirst);
        regGrid.addRow(3, new Label("Surname*:"), regSur);
        regGrid.addRow(4, new Label("Contact:"), regContact);
        regGrid.addRow(5, new Label("Physical Address*:"), regAddr);

        Button regBtn = new Button("Register Customer");
        regBtn.setOnAction(e -> {
            try {
                Customer created = loginController.registerCustomer(
                        regUser.getText(), regPass.getText(), regFirst.getText(),
                        regSur.getText(), regContact.getText(), regAddr.getText()
                );
                new AccountView(stage, created).show();
            } catch (Exception ex) {
                regMsg.setStyle("-fx-text-fill: red;");
                regMsg.setText(ex.getMessage());
            }
        });

        regGrid.add(regBtn, 1, 6);
        regGrid.add(regMsg, 0, 7, 2, 1);

        Tab regTab = new Tab("New Customer Registration", regGrid);
        regTab.setClosable(false);

        tabPane.getTabs().addAll(loginTab, regTab);
        Scene scene = new Scene(tabPane, 460, 420);
        stage.setTitle("BAC OOAD Banking System");
        stage.setScene(scene);
        stage.show();
    }
}