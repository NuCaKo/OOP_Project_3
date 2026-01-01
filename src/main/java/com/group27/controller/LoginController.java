package com.group27.controller;

import com.group27.core.DatabaseAdapter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private Button actionButton;
    @FXML private Button toggleButton;
    @FXML private VBox registerPane;

    private boolean isLoginMode = true;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("customer", "carrier", "owner");
        roleComboBox.setValue("customer");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields.");
            return;
        }

        DatabaseAdapter db = DatabaseAdapter.getInstance();

        if (isLoginMode) {
            if (db.login(username, password)) {
                com.group27.model.User user = db.getUserByUsername(username);
                com.group27.utils.UserSession.getInstance().setCurrentUser(user);
                
                String role = user.getRole(); // Assuming role in user object is correct
                navigateToRole(role);
            } else {
                showError("Invalid credentials.");
            }
        } else {
            // Register
            String address = addressField.getText();
            String role = roleComboBox.getValue();
            
            if (address.isEmpty()) {
                showError("Address is required for registration.");
                return;
            }

            // Strong password check (simple length check for now)
            if (password.length() < 4) {
                 showError("Password must be at least 4 characters.");
                 return;
            }

            if (db.registerUser(username, password, role, address)) {
                showError("Registration successful! Please login."); // Using error label for success msg momentarily
                errorLabel.setStyle("-fx-text-fill: green;");
                toggleMode();
            } else {
                showError("Registration failed. Username may be taken.");
            }
        }
    }

    @FXML
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            actionButton.setText("Login");
            toggleButton.setText("Don't have an account? Register");
            registerPane.setVisible(false);
            registerPane.setManaged(false);
            errorLabel.setVisible(false);
        } else {
            actionButton.setText("Register");
            toggleButton.setText("Already have an account? Login");
            registerPane.setVisible(true);
            registerPane.setManaged(true);
            errorLabel.setVisible(false);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #dc3545;");
        errorLabel.setVisible(true);
    }

    private void navigateToRole(String role) {
        try {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            FXMLLoader loader;
            String title = "Group27 GreenGrocer";
            
            if ("customer".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/customer_main.fxml"));
            } else if ("owner".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/owner.fxml"));
            } else if ("carrier".equalsIgnoreCase(role)) {
                loader = new FXMLLoader(getClass().getResource("/fxml/carrier.fxml"));
            } else {
                showError("Unknown role: " + role);
                return;
            }

            Scene scene = new Scene(loader.load(), 960, 540);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load interface: " + e.getMessage());
        }
    }
}
