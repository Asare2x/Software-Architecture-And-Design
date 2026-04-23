package com.example.trading;

import com.example.trading.service.IAuthService;
import com.example.trading.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML private Label welcomeText;

    private IAuthService authService;

    @FXML
    private void initialize() {
        authService = AuthenticationService.getInstance();
        if (authService.isLoggedIn()) {
            welcomeText.setText("Welcome, " + authService.getCurrentUser() + "!");
        }
    }

    @FXML
    protected void onLogoutButtonClick() throws IOException {
        authService.logout();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 400));
        stage.setTitle("Trading System - Login");
    }
}
