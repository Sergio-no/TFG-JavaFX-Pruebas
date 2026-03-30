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

        // Validación básica en cliente
        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce email y contraseña.");
            return;
        }

        setLoading(true);

        // Llamada en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                String token = authService.login(email, password);
                // Guardamos el token en la sesión global
                UserSesion.getInstance().setToken(token);
                UserSesion.getInstance().setEmail(email);

                Platform.runLater(() -> {
                    setLoading(false);
                    AutoEliteApp.navigateTo("dashboard");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Credenciales incorrectas o error de conexión.");
                });
            }
        }).start();
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