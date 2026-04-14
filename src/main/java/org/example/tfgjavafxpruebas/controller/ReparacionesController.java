package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Reparacion;
import org.example.tfgjavafxpruebas.service.ReparacionesService;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.*;

public class ReparacionesController extends BaseController implements Initializable {

    @FXML private TableView<Reparacion> reparacionesTable;
    @FXML private TableColumn<Reparacion, Long> colId;
    @FXML private TableColumn<Reparacion, String> colVehiculo;
    @FXML private TableColumn<Reparacion, String> colMecanico;
    @FXML private TableColumn<Reparacion, String> colFechaInicio;
    @FXML private TableColumn<Reparacion, String> colFechaFin;
    @FXML private TableColumn<Reparacion, String> colEstado;
    @FXML private TableColumn<Reparacion, String> colCoste;
    @FXML private TableColumn<Reparacion, Void>   colAcciones;

    private final ReparacionesService service = new ReparacionesService();
    private final ObservableList<Reparacion> todas = FXCollections.observableArrayList();
    private FilteredList<Reparacion> filtradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        filtradas = new FilteredList<>(todas, p -> true);
        reparacionesTable.setItems(filtradas);
        configurarColumnas();
        cargarAsync();
    }

    private void configurarColumnas() {
        colId         .setCellValueFactory(new PropertyValueFactory<>("id"));
        colVehiculo   .setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colMecanico   .setCellValueFactory(new PropertyValueFactory<>("mecanico"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin   .setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colEstado     .setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCoste      .setCellValueFactory(new PropertyValueFactory<>("costeTotal"));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button terminar  = new Button("Terminada");
            final Button confirmar = new Button("Confirmar");
            final HBox box = new HBox(6, terminar, confirmar);
            {
                terminar .getStyleClass().add("btn-secondary");
                confirmar.getStyleClass().add("btn-primary");
                terminar .setStyle("-fx-padding:4 8; -fx-font-size:11px;");
                confirmar.setStyle("-fx-padding:4 8; -fx-font-size:11px;");
                terminar.setOnAction(e -> {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    accion(() -> service.cambiarEstado(r.getId(), "TERMINADA"));
                });
                confirmar.setOnAction(e -> {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    accion(() -> service.cambiarEstado(r.getId(), "CONFIRMADA"));
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Reparacion> list = service.getAll();
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

    @FXML private void filtrarTodas()       { filtradas.setPredicate(r -> true); }
    @FXML private void filtrarEnProceso()   { filtradas.setPredicate(r -> "EN_PROCESO".equals(r.getEstado())); }
    @FXML private void filtrarTerminadas()  { filtradas.setPredicate(r -> "TERMINADA".equals(r.getEstado())); }
    @FXML private void filtrarConfirmadas() { filtradas.setPredicate(r -> "CONFIRMADA".equals(r.getEstado())); }

    @FXML private void abrirNuevaReparacion() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva reparación");
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1d23;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField vehiculoId  = campo("ID Vehículo");
        TextField mecanicoId  = campo("ID Mecánico");
        TextField citaId      = campo("ID Cita (opcional)");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                lbl("ID Vehículo"), vehiculoId,
                lbl("ID Mecánico"), mecanicoId,
                lbl("ID Cita"),     citaId
        );
        content.setStyle("-fx-padding:16;");
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                Map<String, Object> body = new HashMap<>();
                body.put("vehiculoId",  Long.parseLong(vehiculoId.getText()));
                body.put("mecanicoId",  Long.parseLong(mecanicoId.getText()));
                if (!citaId.getText().isBlank())
                    body.put("citaId", Long.parseLong(citaId.getText()));
                accion(() -> service.crear(body));
            }
            return null;
        });
        dialog.showAndWait();
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("input-field");
        return tf;
    }
    private Label lbl(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;");
        return l;
    }

    private void datosPrueba() {
        todas.setAll(
                new Reparacion(1L, "Seat Ibiza · Carlos M.", "Juan Ramos",  "2026-03-27","",         "EN_PROCESO","320€"),
                new Reparacion(2L, "Ford Focus · Ana L.",    "Miguel Torres","2026-03-26","",         "EN_PROCESO","480€"),
                new Reparacion(3L, "Renault Megane",         "Juan Ramos",  "2026-03-25","2026-03-27","TERMINADA", "180€")
        );
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
