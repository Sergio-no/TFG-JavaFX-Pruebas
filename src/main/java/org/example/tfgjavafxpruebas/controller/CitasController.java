package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Cita;
import org.example.tfgjavafxpruebas.service.CitaService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

import java.net.URL;
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
        // TODO: abrir diálogo de nueva cita
        mostrarDialogoNuevaCita();
    }

    @FXML private void filtrarTodas()       { filtradas.setPredicate(c -> true); }
    @FXML private void filtrarPendientes()  { filtradas.setPredicate(c -> "PENDIENTE".equals(c.getEstado())); }
    @FXML private void filtrarConfirmadas() { filtradas.setPredicate(c -> "CONFIRMADA".equals(c.getEstado())); }
    @FXML private void filtrarCanceladas()  { filtradas.setPredicate(c -> "CANCELADA".equals(c.getEstado())); }

    private void mostrarDialogoNuevaCita() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva cita");
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1d23;");
        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL);

        // Campos básicos
        TextField clienteIdField = new TextField();
        clienteIdField.setPromptText("ID del cliente");
        clienteIdField.getStyleClass().add("input-field");

        TextField vehiculoIdField = new TextField();
        vehiculoIdField.setPromptText("ID del vehículo");
        vehiculoIdField.getStyleClass().add("input-field");

        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setStyle("-fx-background-color:#12151a; -fx-text-fill:#e2e8f0;");

        TextField horaField = new TextField();
        horaField.setPromptText("09:00");
        horaField.getStyleClass().add("input-field");

        TextField tipoField = new TextField();
        tipoField.setPromptText("Revisión, Frenos, Aceite...");
        tipoField.getStyleClass().add("input-field");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                label("ID Cliente"), clienteIdField,
                label("ID Vehículo"), vehiculoIdField,
                label("Fecha"), fechaPicker,
                label("Hora"), horaField,
                label("Tipo de servicio"), tipoField
        );
        content.setStyle("-fx-padding: 16;");
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                accion(() -> service.crear(java.util.Map.of(
                        "clienteId",  Long.parseLong(clienteIdField.getText()),
                        "vehiculoId", Long.parseLong(vehiculoIdField.getText()),
                        "fecha",      fechaPicker.getValue().toString() + "T" + horaField.getText() + ":00",
                        "descripcion", tipoField.getText()
                )));
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
