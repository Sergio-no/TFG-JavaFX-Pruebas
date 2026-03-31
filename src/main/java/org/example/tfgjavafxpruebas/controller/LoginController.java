package org.example.tfgjavafxpruebas.controller;


import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.service.AuthService;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce email y contraseña.");
            return;
        }

        setLoading(true);

        // En modo test — guardamos email y navegamos directamente
        UserSesion.getInstance().setToken("test-token");
        UserSesion.getInstance().setEmail(email);
        UserSesion.getInstance().setRol("JEFE");

        setLoading(false);
        AutoEliteApp.navigateTo("dashboard");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Entrando..." : "Entrar");
    }
}