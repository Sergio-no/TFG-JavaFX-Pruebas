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
import javafx.stage.FileChooser;
import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

import java.io.File;
import java.io.FileOutputStream;
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
    @FXML private Label pendienteLabel;

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
            final Button pagar     = new Button("Pagar");
            final Button descargar = new Button("PDF");
            final HBox box = new HBox(6, pagar, descargar);
            {
                pagar.getStyleClass().add("btn-primary");
                pagar.setStyle("-fx-padding:4 10; -fx-font-size:11px;");
                descargar.getStyleClass().add("btn-secondary");
                descargar.setStyle("-fx-padding:4 10; -fx-font-size:11px;");

                pagar.setOnAction(e -> {
                    Factura f = getTableView().getItems().get(getIndex());
                    if ("PAGADA".equals(f.getEstado())) return;
                    if (ConfirmDialog.ask("Marcar como pagada",
                            "¿Marcar la factura " + f.getNumeroFactura() + " como pagada?")) {
                        mostrarDialogoPago(f.getId());
                    }
                });

                descargar.setOnAction(e -> {
                    Factura f = getTableView().getItems().get(getIndex());
                    descargarPdf(f.getId(), f.getNumeroFactura());
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

    private void descargarPdf(Long id, String numeroFactura) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar factura PDF");
        fileChooser.setInitialFileName(numeroFactura + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        File archivo = fileChooser.showSaveDialog(
                AutoEliteApp.primaryStage);

        if (archivo == null) return; // Usuario canceló

        new Thread(() -> {
            try {
                byte[] pdf = service.descargarPdf(id);
                try (FileOutputStream fos = new FileOutputStream(archivo)) {
                    fos.write(pdf);
                }
                Platform.runLater(() ->
                        ConfirmDialog.info("PDF guardado",
                                "Factura guardada en:\n" + archivo.getAbsolutePath()));
            } catch (Exception ex) {
                Platform.runLater(() ->
                        ConfirmDialog.error("Error al descargar PDF",
                                ex.getMessage()));
            }
        }).start();
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
                    actualizarTotales();
                });
            } catch (Exception e) {
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    private void actualizarTotales() {
        double cobrado = todas.stream()
                .filter(f -> "PAGADA".equals(f.getEstado()))
                .mapToDouble(f -> parsearTotal(f.getTotal()))
                .sum();

        double pendiente = todas.stream()
                .filter(f -> "PENDIENTE".equals(f.getEstado()))
                .mapToDouble(f -> parsearTotal(f.getTotal()))
                .sum();

        totalLabel.setText(String.format("Total cobrado: %.2f€", cobrado));
        if (pendienteLabel != null) {
            pendienteLabel.setText(String.format("Pendiente: %.2f€", pendiente));
        }
    }

    private double parsearTotal(String total) {
        try {
            return Double.parseDouble(total.replace("€", "").replace(",", ".").trim());
        } catch (Exception ex) {
            return 0;
        }
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
        actualizarTotales();
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
