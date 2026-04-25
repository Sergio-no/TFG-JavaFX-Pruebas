package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Cita;
import org.example.tfgjavafxpruebas.service.CitaService;
import org.example.tfgjavafxpruebas.service.VehiculosService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.tfgjavafxpruebas.service.ClientesService;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class CitasController extends BaseController implements Initializable {

    @FXML private TableView<Cita> citasTable;
    @FXML private TableColumn<Cita, Long> colId;
    @FXML private TableColumn<Cita, String> colCliente;
    @FXML private TableColumn<Cita, String> colVehiculo;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colTipo;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TableColumn<Cita, Void>   colAcciones;
    @FXML private TextField searchField;

    private final CitaService service = new CitaService();
    private final VehiculosService vehiculosService = new VehiculosService();
    private ObservableList<Cita> todas = FXCollections.observableArrayList();
    private FilteredList<Cita> filtradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        configurarColumnas();
        configurarBusqueda();
        cargarAsync();
    }

    private void configurarColumnas() {
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente .setCellValueFactory(new PropertyValueFactory<>("clienteNombre"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colFecha   .setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora    .setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTipo    .setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colEstado  .setCellValueFactory(new PropertyValueFactory<>("estado"));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button confirmar = new Button("Confirmar");
            final Button cancelar  = new Button("Cancelar");
            final HBox box = new HBox(6, confirmar, cancelar);
            {
                confirmar.getStyleClass().add("btn-primary");
                cancelar .getStyleClass().add("btn-secondary");
                confirmar.setStyle("-fx-padding: 4 10; -fx-font-size:11px;");
                cancelar .setStyle("-fx-padding: 4 10; -fx-font-size:11px;");
                confirmar.setOnAction(e -> {
                    Cita c = getTableView().getItems().get(getIndex());
                    if (ConfirmDialog.ask("Confirmar cita",
                            "¿Confirmar la cita de " + c.getClienteNombre() + "?")) {
                        accion(() -> service.confirmar(c.getId()));
                    }
                });
                cancelar.setOnAction(e -> {
                    Cita c = getTableView().getItems().get(getIndex());
                    if (ConfirmDialog.ask("Cancelar cita",
                            "¿Seguro que quieres cancelar la cita de " + c.getClienteNombre() + "?")) {
                        accion(() -> service.cancelar(c.getId()));
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configurarBusqueda() {
        filtradas = new FilteredList<>(todas, p -> true);
        searchField.textProperty().addListener((obs, old, val) ->
                filtradas.setPredicate(c -> val == null || val.isEmpty() ||
                        c.getClienteNombre().toLowerCase().contains(val.toLowerCase()))
        );
        citasTable.setItems(filtradas);
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Cita> list = service.getAll();
                Platform.runLater(() -> todas.setAll(list));
            } catch (Exception e) {
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    private void accion(RunnableEx r) {
        new Thread(() -> {
            try { r.run(); Platform.runLater(this::cargarAsync); }
            catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    @FXML private void abrirNuevaCita() {
        mostrarDialogoNuevaCita();
    }

    @FXML private void filtrarTodas()       { filtradas.setPredicate(c -> true); }
    @FXML private void filtrarPendientes()  { filtradas.setPredicate(c -> "PENDIENTE".equals(c.getEstado())); }
    @FXML private void filtrarConfirmadas() { filtradas.setPredicate(c -> "CONFIRMADA".equals(c.getEstado())); }
    @FXML private void filtrarCanceladas()  { filtradas.setPredicate(c -> "CANCELADA".equals(c.getEstado())); }

    private void mostrarDialogoNuevaCita() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva cita");
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1d23; -fx-min-width:500;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Cliente combo ──
        ComboBox<ClientesService.ClienteItem> clienteCombo = new ComboBox<>();
        clienteCombo.getStyleClass().add("input-field");
        clienteCombo.setMaxWidth(Double.MAX_VALUE);
        clienteCombo.setPromptText("Selecciona cliente...");

        new Thread(() -> {
            try {
                var lista = new ClientesService().getAllLite();
                javafx.application.Platform.runLater(() ->
                        clienteCombo.getItems().setAll(lista));
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        // ── Matrícula con botón buscar (igual que en reparaciones) ──
        TextField matriculaField = new TextField();
        matriculaField.setPromptText("Ej: 1234ABC");
        matriculaField.getStyleClass().add("input-field");

        Button buscarBtn = new Button("Buscar");
        buscarBtn.getStyleClass().add("btn-primary");
        buscarBtn.setStyle("-fx-padding:8 16; -fx-font-size:12px;");
        Label vehiculoInfo = new Label("Introduce una matrícula y pulsa Buscar");
        vehiculoInfo.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
        vehiculoInfo.setWrapText(true);
        HBox matriculaBox = new HBox(8, matriculaField, buscarBtn);
        HBox.setHgrow(matriculaField, Priority.ALWAYS);

        final VehiculosService.VehiculoItem[] vehiculoEncontrado = {null};

        buscarBtn.setOnAction(e -> {
            String mat = matriculaField.getText().trim();
            if (mat.isEmpty()) {
                vehiculoInfo.setText("Introduce una matrícula.");
                vehiculoInfo.setStyle("-fx-text-fill:#f87171; -fx-font-size:11px;");
                return;
            }
            vehiculoInfo.setText("Buscando...");
            vehiculoInfo.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
            new Thread(() -> {
                try {
                    VehiculosService.VehiculoItem v = vehiculosService.getByMatricula(mat);
                    vehiculoEncontrado[0] = v;
                    Platform.runLater(() -> {
                        vehiculoInfo.setText("✓ " + v.getDescripcion()
                                + " — Cliente: " + v.getClienteNombre());
                        vehiculoInfo.setStyle(
                                "-fx-text-fill:#4ade80; -fx-font-size:11px;");
                    });
                } catch (Exception ex) {
                    vehiculoEncontrado[0] = null;
                    Platform.runLater(() -> {
                        vehiculoInfo.setText("✗ " + ex.getMessage());
                        vehiculoInfo.setStyle(
                                "-fx-text-fill:#f87171; -fx-font-size:11px;");
                    });
                }
            }).start();
        });

        // ── Fecha con horas disponibles ──
        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setStyle("-fx-background-color:#12151a; -fx-text-fill:#e2e8f0;");

        ComboBox<String> horaCombo = new ComboBox<>();
        horaCombo.getStyleClass().add("input-field");
        horaCombo.setMaxWidth(Double.MAX_VALUE);
        horaCombo.setPromptText("Selecciona primero una fecha");
        horaCombo.setDisable(true);

        Label horasInfo = new Label("");
        horasInfo.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");

        // Cuando cambie la fecha, cargar horas disponibles
        fechaPicker.valueProperty().addListener((obs, old, fecha) -> {
            if (fecha == null) {
                horaCombo.setDisable(true);
                horaCombo.getItems().clear();
                horaCombo.setPromptText("Selecciona primero una fecha");
                return;
            }
            horaCombo.setDisable(true);
            horaCombo.getItems().clear();
            horaCombo.setPromptText("Cargando horas...");
            horasInfo.setText("Consultando disponibilidad...");

            new Thread(() -> {
                try {
                    List<String> horas = service.getHorasDisponibles(fecha);
                    Platform.runLater(() -> {
                        horaCombo.getItems().setAll(horas);
                        horaCombo.setDisable(false);
                        horaCombo.setPromptText("Selecciona hora");
                        if (horas.isEmpty()) {
                            horasInfo.setText("No hay horas disponibles para este día");
                            horasInfo.setStyle("-fx-text-fill:#f87171; -fx-font-size:11px;");
                        } else {
                            horasInfo.setText(horas.size() + " horas disponibles");
                            horasInfo.setStyle("-fx-text-fill:#4ade80; -fx-font-size:11px;");
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        // Fallback: generar horas localmente
                        horaCombo.getItems().clear();
                        for (int h = 9; h < 18; h++) {
                            horaCombo.getItems().add(String.format("%02d:00", h));
                            horaCombo.getItems().add(String.format("%02d:30", h));
                        }
                        horaCombo.setDisable(false);
                        horaCombo.setPromptText("Selecciona hora");
                        horasInfo.setText("Sin validación de disponibilidad");
                        horasInfo.setStyle("-fx-text-fill:#d4a72c; -fx-font-size:11px;");
                    });
                }
            }).start();
        });

        TextField tipoField = new TextField();
        tipoField.setPromptText("Revisión, Frenos, Aceite...");
        tipoField.getStyleClass().add("input-field");

        VBox content = new VBox(10,
                label("Cliente"),             clienteCombo,
                label("Matrícula del vehículo"), matriculaBox, vehiculoInfo,
                new Separator(),
                label("Fecha"),               fechaPicker,
                label("Hora"),                horaCombo, horasInfo,
                label("Tipo de servicio"),    tipoField
        );
        content.setStyle("-fx-padding: 16;");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#1a1d23; -fx-background-color:#1a1d23;");
        scroll.setPrefHeight(480);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (clienteCombo.getValue() == null) {
                    ConfirmDialog.error("Campo obligatorio", "Selecciona un cliente.");
                    return null;
                }
                if (vehiculoEncontrado[0] == null) {
                    ConfirmDialog.error("Vehículo no encontrado",
                            "Busca un vehículo válido por matrícula.");
                    return null;
                }
                if (fechaPicker.getValue() == null || horaCombo.getValue() == null) {
                    ConfirmDialog.error("Campos obligatorios",
                            "Selecciona fecha y hora.");
                    return null;
                }
                try {
                    String fechaHora = fechaPicker.getValue().toString() + "T"
                            + horaCombo.getValue() + ":00";

                    accion(() -> service.crear(java.util.Map.of(
                            "clienteId",   clienteCombo.getValue().getId(),
                            "matricula",   matriculaField.getText().trim().toUpperCase(),
                            "fecha",       fechaHora,
                            "descripcion", tipoField.getText().trim()
                    )));
                } catch (Exception ex) {
                    ConfirmDialog.error("Error", ex.getMessage());
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private Label label(String txt) {
        Label l = new Label(txt);
        l.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        return l;
    }

    private void datosPrueba() {
        todas.setAll(
                new Cita(1L, "Carlos Martín",  "Seat Ibiza · 4521KLM",     "2026-03-27","09:00","Revisión",   "CONFIRMADA",""),
                new Cita(2L, "Ana López",      "Ford Focus · 8820BCN",     "2026-03-27","10:30","Frenos",     "PENDIENTE", ""),
                new Cita(3L, "Taller Norte",   "Renault Megane · 3312ZZT", "2026-03-27","12:00","Diagnóstico","CONFIRMADA",""),
                new Cita(4L, "Pedro García",   "VW Golf · 7714MAD",        "2026-03-27","16:00","Aceite",     "PENDIENTE", "")
        );
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
