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
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private boolean passwordShowing = false;
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Sincronizar campos de contraseña
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordShowing = !passwordShowing;

        if (passwordShowing) {
            // Mostrar contraseña en texto plano
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            togglePasswordBtn.setText("🙈");
        } else {
            // Ocultar contraseña
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        // Leer del campo activo
        String password = passwordShowing
                ? passwordVisible.getText()
                : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce email y contraseña.");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                String token = authService.login(email, password);
                String uid   = authService.getUid(token);
                String rol   = authService.getRolFromBackend(token);

                if ("CLIENTE".equals(rol)) {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Los clientes no pueden acceder al panel de gestión.\n"
                                + "Usa la aplicación móvil.");
                    });
                    return;
                }

                UserSesion.getInstance().setToken(token);
                UserSesion.getInstance().setEmail(email);
                UserSesion.getInstance().setUid(uid);
                UserSesion.getInstance().setRol(rol);

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

    @FXML
    public void goToRegister() {
        AutoEliteApp.navigateTo("register");
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
