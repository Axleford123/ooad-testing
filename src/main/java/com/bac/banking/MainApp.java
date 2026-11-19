package com.bac.banking;

import com.bac.banking.dao.DatabaseManager;
import com.bac.banking.view.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.initializeDatabase();
        new LoginView(primaryStage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}