package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Cliente;
import org.example.tfgjavafxpruebas.service.ClientesService;
import org.example.tfgjavafxpruebas.service.VehiculosService;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ClientesController extends BaseController implements Initializable {

    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, Long>    colId;
    @FXML private TableColumn<Cliente, String>  colNombre;
    @FXML private TableColumn<Cliente, String>  colEmail;
    @FXML private TableColumn<Cliente, String>  colTelefono;
    @FXML private TableColumn<Cliente, Integer> colPuntos;
    @FXML private TableColumn<Cliente, String>  colGastado;
    @FXML private TableColumn<Cliente, String>  colRegistro;
    @FXML private TextField searchField;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVehiculos;

    private final ClientesService service = new ClientesService();
    private final VehiculosService vehiculosService = new VehiculosService();
    private final ObservableList<Cliente> todos = FXCollections.observableArrayList();
    private FilteredList<Cliente> filtrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        filtrados = new FilteredList<>(todos, p -> true);
        clientesTable.setItems(filtrados);
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colPuntos  .setCellValueFactory(new PropertyValueFactory<>("puntosAcumulados"));
        colGastado .setCellValueFactory(new PropertyValueFactory<>("totalGastado"));
        colRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        searchField.textProperty().addListener((obs, old, val) ->
                filtrados.setPredicate(c -> val == null || val.isEmpty() ||
                        c.getNombre().toLowerCase().contains(val.toLowerCase()) ||
                        c.getEmail().toLowerCase().contains(val.toLowerCase())));

        boolean esJefe = UserSesion.getInstance().isJefe();
        btnNuevo   .setVisible(esJefe);
        btnEditar  .setVisible(esJefe);
        btnEliminar.setVisible(esJefe);
        btnNuevo   .setManaged(esJefe);
        btnEditar  .setManaged(esJefe);
        btnEliminar.setManaged(esJefe);

        cargarAsync();
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Cliente> list = service.getAll();
                Platform.runLater(() -> todos.setAll(list));
            } catch (Exception e) {
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    @FXML private void abrirNuevoCliente() { mostrarDialogoCliente(null); }

    @FXML
    private void abrirEditarCliente() {
        Cliente sel = clientesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            ConfirmDialog.error("Sin selección", "Selecciona un cliente de la tabla.");
            return;
        }
        mostrarDialogoCliente(sel);
    }

    @FXML
    private void eliminarClienteSeleccionado() {
        Cliente sel = clientesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            ConfirmDialog.error("Sin selección", "Selecciona un cliente de la tabla.");
            return;
        }
        if (!ConfirmDialog.ask("Eliminar cliente",
                "¿Seguro que quieres eliminar a " + sel.getNombre() + "?\n"
                        + "El cliente será marcado como inactivo."))
            return;

        new Thread(() -> {
            try {
                service.eliminar(sel.getId());
                Platform.runLater(this::cargarAsync);
            } catch (Exception ex) {
                Platform.runLater(() -> ConfirmDialog.error("Error", ex.getMessage()));
            }
        }).start();
    }

    @FXML
    private void verVehiculosCliente() {
        Cliente sel = clientesTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            ConfirmDialog.error("Sin selección", "Selecciona un cliente de la tabla.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Vehículos de " + sel.getNombre());
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1d23; -fx-min-width:500;");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox vehiculosContainer = new VBox(8);
        vehiculosContainer.setStyle("-fx-padding:16;");
        Label cargando = new Label("Cargando vehículos...");
        cargando.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
        vehiculosContainer.getChildren().add(cargando);

        ScrollPane scroll = new ScrollPane(vehiculosContainer);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#1a1d23; -fx-background-color:#1a1d23;");
        scroll.setPrefHeight(300);
        dialog.getDialogPane().setContent(scroll);

        new Thread(() -> {
            try {
                List<VehiculosService.VehiculoItem> vehiculos =
                        vehiculosService.getByCliente(sel.getId());
                Platform.runLater(() -> {
                    vehiculosContainer.getChildren().clear();
                    if (vehiculos.isEmpty()) {
                        Label empty = new Label("Este cliente no tiene vehículos registrados.");
                        empty.setStyle("-fx-text-fill:#6b7280; -fx-font-size:13px;");
                        vehiculosContainer.getChildren().add(empty);
                    } else {
                        for (VehiculosService.VehiculoItem v : vehiculos) {
                            vehiculosContainer.getChildren().add(crearFilaVehiculo(v));
                        }
                    }
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    vehiculosContainer.getChildren().clear();
                    Label err = new Label("Error: " + ex.getMessage());
                    err.setStyle("-fx-text-fill:#f87171; -fx-font-size:12px;");
                    err.setWrapText(true);
                    vehiculosContainer.getChildren().add(err);
                });
            }
        }).start();

        dialog.showAndWait();
    }

    private HBox crearFilaVehiculo(VehiculosService.VehiculoItem v) {
        Label matricula = new Label(v.getMatricula());
        matricula.setStyle("-fx-text-fill:#7ec8e3; -fx-font-size:14px; -fx-font-weight:bold;");
        Label desc = new Label(v.getDescripcion());
        desc.setStyle("-fx-text-fill:#e2e8f0; -fx-font-size:13px;");
        VBox info = new VBox(2, matricula, desc);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, info, spacer);
        row.setStyle("-fx-background-color:#12151a; -fx-padding:12 16; "
                + "-fx-background-radius:6; -fx-border-color:#2e333d; "
                + "-fx-border-width:1; -fx-border-radius:6;");
        return row;
    }

    private void mostrarDialogoCliente(Cliente existente) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nuevo cliente" : "Editar cliente");
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1d23;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre    = campo("Nombre");
        TextField apellidos = campo("Apellidos");
        TextField email     = campo("email@ejemplo.com");
        TextField telefono  = campo("600000000");

        if (existente != null) {
            String completo = existente.getNombre();
            int idx = completo.indexOf(' ');
            if (idx > 0) {
                nombre.setText(completo.substring(0, idx));
                apellidos.setText(completo.substring(idx + 1));
            } else {
                nombre.setText(completo);
            }
            email.setText(existente.getEmail());
            telefono.setText("—".equals(existente.getTelefono()) ? "" : existente.getTelefono());
        }

        VBox content = new VBox(10,
                lbl("Nombre *"), nombre, lbl("Apellidos *"), apellidos,
                lbl("Email *"), email, lbl("Teléfono"), telefono);
        content.setStyle("-fx-padding:16;");
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (nombre.getText().isBlank() || apellidos.getText().isBlank()
                        || email.getText().isBlank()) {
                    ConfirmDialog.error("Campos obligatorios",
                            "Nombre, apellidos y email son obligatorios.");
                    return null;
                }
                Map<String, Object> body = new HashMap<>();
                body.put("nombre",    nombre.getText().trim());
                body.put("apellidos", apellidos.getText().trim());
                body.put("email",     email.getText().trim());
                body.put("telefono",  telefono.getText().trim());

                new Thread(() -> {
                    try {
                        if (existente == null) service.crear(body);
                        else service.actualizar(existente.getId(), body);
                        Platform.runLater(this::cargarAsync);
                    } catch (Exception ex) {
                        Platform.runLater(() -> ConfirmDialog.error("Error", ex.getMessage()));
                    }
                }).start();
            }
            return null;
        });
        dialog.showAndWait();
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

    private void datosPrueba() {
        todos.setAll(
                new Cliente(1L,"Carlos Martín","carlos@email.com",
                        "666111222",340,"1.200€","2025-01-10"),
                new Cliente(2L,"Ana López","ana@email.com",
                        "666333444",820,"2.640€","2024-11-05"),
                new Cliente(3L,"Pedro García","pedro@email.com",
                        "666555666",120,"380€","2026-02-20")
        );
    }
}
