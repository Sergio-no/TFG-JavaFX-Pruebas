package org.example.tfgjavafxpruebas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.service.AuthService;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

public class RegisterController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidosField;
    @FXML private TextField emailField;
    @FXML private TextField telefonoField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> rolCombo;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        rolCombo.getItems().addAll("CLIENTE", "OFICINA", "JEFE");
        rolCombo.setValue("CLIENTE");
    }

    @FXML
    public void handleRegister() {
        String nombre    = nombreField.getText().trim();
        String apellidos = apellidosField.getText().trim();
        String email     = emailField.getText().trim();
        String telefono  = telefonoField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();
        String rol       = rolCombo.getValue();

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Completa los campos obligatorios.");
            return;
        }
        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                // 1. Crear cuenta en Firebase
                authService.signUp(email, password);

                // 2. Obtener UID (reusa login para token limpio)
                String token = authService.login(email, password);
                String uid   = authService.getUid(token);

                // 3. Registrar en backend (esto también fija el custom claim)
                authService.registerInBackend(uid, nombre, apellidos, email, telefono, rol);

                // 4. Re-login para obtener token con el claim actualizado
                String nuevoToken = authService.login(email, password);
                String rolFinal   = authService.getRolFromBackend(nuevoToken);

                UserSesion.getInstance().setToken(nuevoToken);
                UserSesion.getInstance().setEmail(email);
                UserSesion.getInstance().setUid(uid);
                UserSesion.getInstance().setRol(rolFinal);

                Platform.runLater(() -> {
                    setLoading(false);
                    ConfirmDialog.info("Cuenta creada",
                            "Bienvenido a AutoElite, " + nombre + ".");
                    AutoEliteApp.navigateTo("dashboard");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError(e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    public void goToLogin() {
        AutoEliteApp.navigateTo("login");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        registerButton.setDisable(loading);
        registerButton.setText(loading ? "Creando..." : "Crear cuenta");
    }
}
