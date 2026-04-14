package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Factura;
import org.example.tfgjavafxpruebas.service.FacturasService;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

import java.net.URL;
import java.util.*;

public class FacturasController extends BaseController implements Initializable {

    @FXML private TableView<Factura> facturasTable;
    @FXML private TableColumn<Factura, Long> colId;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colTotal;
    @FXML private TableColumn<Factura, String> colMetodo;
    @FXML private TableColumn<Factura, String> colEstado;
    @FXML private TableColumn<Factura, Void>   colAcciones;
    @FXML private Label totalLabel;

    private final FacturasService service = new FacturasService();
    private final ObservableList<Factura> todas = FXCollections.observableArrayList();
    private FilteredList<Factura> filtradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        filtradas = new FilteredList<>(todas, p -> true);
        facturasTable.setItems(filtradas);
        configurarColumnas();
        cargarAsync();
    }

    private void configurarColumnas() {
        colId    .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero .setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colFecha  .setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTotal  .setCellValueFactory(new PropertyValueFactory<>("total"));
        colMetodo .setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colEstado .setCellValueFactory(new PropertyValueFactory<>("estado"));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button pagar = new Button("Marcar pagada");
            final HBox box = new HBox(pagar);
            {
                pagar.getStyleClass().add("btn-primary");
                pagar.setStyle("-fx-padding:4 10; -fx-font-size:11px;");
                pagar.setOnAction(e -> {
                    Factura f = getTableView().getItems().get(getIndex());
                    if ("PAGADA".equals(f.getEstado())) return;
                    if (ConfirmDialog.ask("Marcar como pagada",
                            "¿Marcar la factura " + f.getNumeroFactura() + " como pagada?")) {
                        mostrarDialogoPago(f.getId());
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Factura f = getTableView().getItems().get(getIndex());
                pagar.setDisable("PAGADA".equals(f.getEstado()));
                setGraphic(box);
            }
        });
    }

    private void mostrarDialogoPago(Long id) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "EFECTIVO", "EFECTIVO", "TARJETA", "TRANSFERENCIA");
        dialog.setTitle("Registrar pago");
        dialog.setHeaderText("Selecciona el método de pago");
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1d23;");
        dialog.showAndWait().ifPresent(metodo ->
                accion(() -> service.marcarPagada(id, metodo)));
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Factura> list = service.getAll();
                Platform.runLater(() -> {
                    todas.setAll(list);
                    actualizarTotal();
                });
            } catch (Exception e) {
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    private void actualizarTotal() {
        double sum = todas.stream()
                .filter(f -> "PAGADA".equals(f.getEstado()))
                .mapToDouble(f -> {
                    try { return Double.parseDouble(f.getTotal().replace("€","")); }
                    catch (Exception ex) { return 0; }
                }).sum();
        totalLabel.setText(String.format("Total cobrado: %.2f€", sum));
    }

    private void accion(RunnableEx r) {
        new Thread(() -> {
            try { r.run(); Platform.runLater(this::cargarAsync); }
            catch (Exception ex) { ex.printStackTrace(); }
        }).start();
    }

    @FXML private void filtrarTodas()      { filtradas.setPredicate(f -> true); }
    @FXML private void filtrarPendientes() { filtradas.setPredicate(f -> "PENDIENTE".equals(f.getEstado())); }
    @FXML private void filtrarPagadas()    { filtradas.setPredicate(f -> "PAGADA".equals(f.getEstado())); }

    private void datosPrueba() {
        todas.setAll(
                new Factura(1L,"FAC-2026-0041","Ana López",    "2026-03-26","480€","TARJETA",     true),
                new Factura(2L,"FAC-2026-0042","Taller Norte", "2026-03-27","180€",null,          false),
                new Factura(3L,"FAC-2026-0043","Carlos Martín","2026-03-27","320€",null,          false)
        );
        actualizarTotal();
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
