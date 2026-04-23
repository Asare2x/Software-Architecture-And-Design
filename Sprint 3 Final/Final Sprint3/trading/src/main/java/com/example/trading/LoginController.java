package com.example.trading;

import com.example.trading.service.AuthenticationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;
    @FXML private Button        loginButton;

    private AuthenticationService authService;

    public void initialize() {
        authService = AuthenticationService.getInstance();
        statusLabel.setText("");
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        Platform.runLater(() -> usernameField.requestFocus());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        statusLabel.setText("");

        if (username == null || username.trim().isEmpty()) {
            showError("Please enter a username"); return;
        }
        if (password == null || password.trim().isEmpty()) {
            showError("Please enter a password"); return;
        }

        loginButton.setDisable(true);
        statusLabel.setStyle("-fx-text-fill: blue;");
        statusLabel.setText("Authenticating...");

        try {
            if (authService.authenticate(username.trim(), password)) {
                showSuccess("Welcome, " + username + "!");
                Platform.runLater(this::openMainApplication);
            } else {
                showError("Invalid username or password");
                passwordField.clear();
                passwordField.requestFocus();
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        } finally {
            loginButton.setDisable(false);
        }
    }

    private void openMainApplication() {
        try {
            Stage mainStage = new Stage();
            TradingDashboard dashboard = new TradingDashboard(
                    mainStage, authService);
            dashboard.show();

            // close login window
            Stage loginStage = (Stage) loginButton.getScene().getWindow();
            loginStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog(e);
        }
    }

    @FXML
    private void handleExit() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Exit"); a.setContentText("Are you sure?");
        a.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { Platform.exit(); System.exit(0); }
        });
    }

    @FXML
    private void handleRegister() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Demo Accounts");
        a.setHeaderText("Available Test Accounts");
        a.setContentText("admin / admin123\nuser / password\ntest / test123");
        a.showAndWait();
    }

    private void showErrorDialog(Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) cause = cause.getCause();
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Failed to open trading screen");
        TextArea ta = new TextArea(cause.getClass().getName() + ": " + cause.getMessage());
        ta.setEditable(false); ta.setWrapText(true);
        a.getDialogPane().setContent(ta);
        a.getDialogPane().setPrefSize(600, 250);
        a.showAndWait();
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: green;");
    }
}
