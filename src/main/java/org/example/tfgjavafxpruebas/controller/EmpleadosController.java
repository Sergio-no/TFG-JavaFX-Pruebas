package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Empleado;
import org.example.tfgjavafxpruebas.service.AuthService;
import org.example.tfgjavafxpruebas.service.EmpleadosService;
import org.example.tfgjavafxpruebas.service.MecanicosService;
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
import java.util.Map;
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
    private final MecanicosService mecanicosService = new MecanicosService();
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
                List<Empleado> listaCompleta = new java.util.ArrayList<>();

                // Cargar empleados de oficina/jefe
                try {
                    List<EmpleadosService.EmpleadoItem> empleados = service.getAll();
                    for (EmpleadosService.EmpleadoItem e : empleados) {
                        listaCompleta.add(new Empleado(e.getId(), e.getNombre(),
                                e.getEmail(), e.getTelefono(),
                                e.getRol(), e.isActivo()));
                    }
                } catch (Exception ignored) {}

                // Cargar todos los mecánicos (activos e inactivos)
                try {
                    List<MecanicosService.MecanicoItem> mecanicos = mecanicosService.getAll();
                    for (MecanicosService.MecanicoItem m : mecanicos) {
                        listaCompleta.add(new Empleado(m.getId(), m.getNombre(),
                                m.getEspecialidad(), m.getTelefono(),
                                "MECANICO", m.isActivo()));
                    }
                } catch (Exception ignored) {}

                List<Empleado> finalLista = listaCompleta;
                Platform.runLater(() -> todos.setAll(finalLista));
            } catch (Exception e) {
                Platform.runLater(() ->
                        ConfirmDialog.error("Error",
                                "No se pudieron cargar los empleados: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void abrirNuevoEmpleado() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nuevo empleado");
        dialog.getDialogPane().setStyle(
                "-fx-background-color:#1a1d23; -fx-min-width:440;");
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Selector de tipo de empleado ──
        ComboBox<String> tipoCombo = new ComboBox<>();
        tipoCombo.getItems().addAll("Usuario de oficina", "Mecánico de taller");
        tipoCombo.getStyleClass().add("input-field");
        tipoCombo.setMaxWidth(Double.MAX_VALUE);
        tipoCombo.setPromptText("Selecciona tipo de empleado...");

        // Campos comunes
        TextField nombre    = campo("Nombre");
        TextField apellidos = campo("Apellidos");
        TextField telefono  = campo("Teléfono");

        // Campos solo para OFICINA
        TextField email     = campo("email@autoelite.com");
        PasswordField password = new PasswordField();
        password.setPromptText("Mínimo 6 caracteres");
        password.getStyleClass().add("input-field");
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirmar contraseña");
        confirmPass.getStyleClass().add("input-field");

        // Campo solo para MECANICO
        TextField especialidad = campo("Ej: Frenos, Motor, Electricidad...");

        // Labels
        Label lblEmail = lbl("Email *");
        Label lblPassword = lbl("Contraseña *");
        Label lblConfirm = lbl("Confirmar contraseña *");
        Label lblEspecialidad = lbl("Especialidad");

        // Info labels
        Label infoOficina = crearInfoLabel(
                "Se creará un usuario con acceso al panel de gestión (rol OFICINA). "
                        + "Necesita email y contraseña para iniciar sesión.");
        Label infoMecanico = crearInfoLabel(
                "Se registrará un mecánico de taller. No necesita cuenta de usuario "
                        + "ni acceso al panel — solo aparecerá en la lista de mecánicos "
                        + "disponibles para asignar reparaciones.");

        // Contenedor dinámico que cambia según la selección
        VBox camposEspecificos = new VBox(10);

        tipoCombo.valueProperty().addListener((obs, old, val) -> {
            camposEspecificos.getChildren().clear();
            if ("Usuario de oficina".equals(val)) {
                camposEspecificos.getChildren().addAll(
                        lblEmail, email,
                        lblPassword, password,
                        lblConfirm, confirmPass,
                        infoOficina
                );
            } else if ("Mecánico de taller".equals(val)) {
                camposEspecificos.getChildren().addAll(
                        lblEspecialidad, especialidad,
                        infoMecanico
                );
            }
        });

        VBox content = new VBox(10,
                lbl("Tipo de empleado *"), tipoCombo,
                new Separator(),
                lbl("Nombre *"),    nombre,
                lbl("Apellidos *"), apellidos,
                lbl("Teléfono"),    telefono,
                new Separator(),
                camposEspecificos
        );
        content.setStyle("-fx-padding:16;");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#1a1d23; -fx-background-color:#1a1d23;");
        scroll.setPrefHeight(500);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String tipo = tipoCombo.getValue();
                if (tipo == null) {
                    ConfirmDialog.error("Tipo requerido",
                            "Selecciona el tipo de empleado a crear.");
                    return null;
                }

                String nom = nombre.getText().trim();
                String ape = apellidos.getText().trim();
                String tel = telefono.getText().trim();

                if (nom.isEmpty() || ape.isEmpty()) {
                    ConfirmDialog.error("Campos obligatorios",
                            "Nombre y apellidos son obligatorios.");
                    return null;
                }

                if ("Usuario de oficina".equals(tipo)) {
                    crearUsuarioOficina(nom, ape, email.getText().trim(),
                            tel, password.getText(), confirmPass.getText());
                } else {
                    crearMecanico(nom, ape, tel, especialidad.getText().trim());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void crearUsuarioOficina(String nom, String ape, String em,
                                     String tel, String pass, String conf) {
        if (em.isEmpty() || pass.isEmpty()) {
            ConfirmDialog.error("Campos obligatorios",
                    "Email y contraseña son obligatorios para usuarios de oficina.");
            return;
        }
        if (pass.length() < 6) {
            ConfirmDialog.error("Contraseña débil",
                    "La contraseña debe tener al menos 6 caracteres.");
            return;
        }
        if (!pass.equals(conf)) {
            ConfirmDialog.error("Contraseñas",
                    "Las contraseñas no coinciden.");
            return;
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

    private void crearMecanico(String nom, String ape, String tel, String esp) {
        new Thread(() -> {
            try {
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("nombre", nom);
                body.put("apellidos", ape);
                body.put("telefono", tel);
                body.put("especialidad", esp);

                mecanicosService.crear(body);

                Platform.runLater(() -> {
                    ConfirmDialog.info("Mecánico creado",
                            "Se ha registrado al mecánico " + nom + " " + ape
                                    + (esp.isEmpty() ? "" : "\nEspecialidad: " + esp)
                                    + ".");
                    cargarAsync();
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ConfirmDialog.error("Error al crear mecánico",
                                ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void desactivarEmpleado() {
        Empleado sel = empleadosTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            ConfirmDialog.error("Sin selección", "Selecciona un empleado de la tabla.");
            return;
        }

        String accion = sel.isActivo() ? "desactivar" : "reactivar";
        if (!ConfirmDialog.ask("Confirmar acción",
                "¿Seguro que quieres " + accion + " a " + sel.getNombre() + "?"))
            return;

        new Thread(() -> {
            try {
                if ("MECANICO".equals(sel.getRol())) {
                    mecanicosService.toggleActivo(sel.getId());
                } else {
                    if (sel.isActivo()) {
                        service.desactivar(sel.getId());
                    } else {
                        service.activar(sel.getId());
                    }
                }
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
