package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Empleado;
import org.example.tfgjavafxpruebas.service.AuthService;
import org.example.tfgjavafxpruebas.service.EmpleadosService;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class EmpleadosController extends BaseController implements Initializable {

    @FXML private TableView<Empleado> empleadosTable;
    @FXML private TableColumn<Empleado, Long>   colId;
    @FXML private TableColumn<Empleado, String> colNombre;
    @FXML private TableColumn<Empleado, String> colEmail;
    @FXML private TableColumn<Empleado, String> colTelefono;
    @FXML private TableColumn<Empleado, String> colRol;
    @FXML private TableColumn<Empleado, String> colEstado;

    private final EmpleadosService service = new EmpleadosService();
    private final ObservableList<Empleado> todos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();

        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colRol     .setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado  .setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstado()));

        empleadosTable.setItems(todos);
        cargarAsync();
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<EmpleadosService.EmpleadoItem> list = service.getAll();
                List<Empleado> empleados = list.stream()
                        .map(e -> new Empleado(e.getId(), e.getNombre(),
                                e.getEmail(), e.getTelefono(),
                                e.getRol(), e.isActivo()))
                        .toList();
                Platform.runLater(() -> todos.setAll(empleados));
            } catch (Exception e) {
                Platform.runLater(() ->
                        ConfirmDialog.error("Error", "No se pudieron cargar los empleados: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void abrirNuevoEmpleado() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo empleado (Oficina)");
        dialog.getDialogPane().setStyle(
                "-fx-background-color:#1a1d23; -fx-min-width:420;");
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre    = campo("Nombre");
        TextField apellidos = campo("Apellidos");
        TextField email     = campo("email@autoelite.com");
        TextField telefono  = campo("Teléfono");
        PasswordField password = new PasswordField();
        password.setPromptText("Mínimo 6 caracteres");
        password.getStyleClass().add("input-field");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirmar contraseña");
        confirmPass.getStyleClass().add("input-field");

        VBox content = new VBox(10,
                lbl("Nombre *"),               nombre,
                lbl("Apellidos *"),            apellidos,
                lbl("Email *"),                email,
                lbl("Teléfono"),               telefono,
                lbl("Contraseña *"),           password,
                lbl("Confirmar contraseña *"), confirmPass,
                crearInfoLabel("Se creará un usuario con rol OFICINA "
                        + "que podrá acceder al panel de gestión.")
        );
        content.setStyle("-fx-padding:16;");
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String nom = nombre.getText().trim();
                String ape = apellidos.getText().trim();
                String em  = email.getText().trim();
                String tel = telefono.getText().trim();
                String pass = password.getText();
                String conf = confirmPass.getText();

                if (nom.isEmpty() || ape.isEmpty() || em.isEmpty()
                        || pass.isEmpty()) {
                    ConfirmDialog.error("Campos obligatorios",
                            "Nombre, apellidos, email y contraseña son obligatorios.");
                    return null;
                }
                if (pass.length() < 6) {
                    ConfirmDialog.error("Contraseña débil",
                            "La contraseña debe tener al menos 6 caracteres.");
                    return null;
                }
                if (!pass.equals(conf)) {
                    ConfirmDialog.error("Contraseñas",
                            "Las contraseñas no coinciden.");
                    return null;
                }

                new Thread(() -> {
                    try {
                        AuthService auth = new AuthService();
                        auth.signUp(em, pass);
                        String token = auth.login(em, pass);
                        String uid   = auth.getUid(token);
                        auth.registerInBackend(uid, nom, ape, em, tel, "OFICINA");

                        Platform.runLater(() -> {
                            ConfirmDialog.info("Empleado creado",
                                    "Se ha creado la cuenta de " + nom + " " + ape
                                            + " con rol OFICINA.\n\nEmail: " + em);
                            cargarAsync();
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() ->
                                ConfirmDialog.error("Error al crear empleado",
                                        ex.getMessage()));
                    }
                }).start();
            }
            return null;
        });
        dialog.showAndWait();
    }

    @FXML
    private void desactivarEmpleado() {
        Empleado sel = empleadosTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            ConfirmDialog.error("Sin selección", "Selecciona un empleado de la tabla.");
            return;
        }
        if (!ConfirmDialog.ask("Desactivar empleado",
                "¿Seguro que quieres desactivar a " + sel.getNombre() + "?"))
            return;

        new Thread(() -> {
            try {
                service.desactivar(sel.getId());
                Platform.runLater(this::cargarAsync);
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ConfirmDialog.error("Error", ex.getMessage()));
            }
        }).start();
    }

    private TextField campo(String p) {
        TextField tf = new TextField(); tf.setPromptText(p);
        tf.getStyleClass().add("input-field"); return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
        return l;
    }
    private Label crearInfoLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill:#7ec8e3; -fx-font-size:11px; "
                + "-fx-background-color:#1a2535; -fx-padding:8 12; "
                + "-fx-background-radius:4;");
        l.setWrapText(true);
        return l;
    }
}

