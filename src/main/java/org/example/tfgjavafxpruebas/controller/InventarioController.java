package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Pieza;
import org.example.tfgjavafxpruebas.service.InventarioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.*;

public class InventarioController extends BaseController implements Initializable {

    @FXML private TableView<Pieza> piezasTable;
    @FXML private TableColumn<Pieza, Long> colId;
    @FXML private TableColumn<Pieza, String>  colNombre;
    @FXML private TableColumn<Pieza, String>  colPrecio;
    @FXML private TableColumn<Pieza, Integer> colStock;
    @FXML private TableColumn<Pieza, Integer> colMinimo;
    @FXML private TableColumn<Pieza, String>  colEstado;
    @FXML private TableColumn<Pieza, Void>    colAcciones;
    @FXML private TextField searchField;

    private final InventarioService service = new InventarioService();
    private final ObservableList<Pieza> todas = FXCollections.observableArrayList();
    private FilteredList<Pieza> filtradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        filtradas = new FilteredList<>(todas, p -> true);
        piezasTable.setItems(filtradas);
        configurarColumnas();
        configurarBusqueda();
        cargarAsync();
    }

    private void configurarColumnas() {
        colId     .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio .setCellValueFactory(c ->
                new SimpleStringProperty(String.format("%.2f€", c.getValue().getPrecioUnitario())));
        colStock  .setCellValueFactory(new PropertyValueFactory<>("stockActual"));
        colMinimo .setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        colEstado .setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEstadoStock()));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            final Button masBtn   = new Button("+10");
            final Button menosBtn = new Button("-1");
            final HBox box = new HBox(6, masBtn, menosBtn);
            {
                masBtn  .getStyleClass().add("btn-primary");
                menosBtn.getStyleClass().add("btn-secondary");
                masBtn  .setStyle("-fx-padding:4 10; -fx-font-size:11px;");
                menosBtn.setStyle("-fx-padding:4 10; -fx-font-size:11px;");
                masBtn.setOnAction(e -> {
                    Pieza p = getTableView().getItems().get(getIndex());
                    accion(() -> service.ajustarStock(p.getId(), 10));
                });
                menosBtn.setOnAction(e -> {
                    Pieza p = getTableView().getItems().get(getIndex());
                    accion(() -> service.ajustarStock(p.getId(), -1));
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void configurarBusqueda() {
        searchField.textProperty().addListener((obs, old, val) ->
                filtradas.setPredicate(p -> val == null || val.isEmpty() ||
                        p.getNombre().toLowerCase().contains(val.toLowerCase())));
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Pieza> list = service.getAll();
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

    @FXML private void abrirNuevaPieza() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva pieza");
        dialog.getDialogPane().setStyle("-fx-background-color:#1a1d23;");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre  = campo("Nombre de la pieza");
        TextField desc    = campo("Descripción");
        TextField precio  = campo("Precio unitario (ej: 12.50)");
        TextField stock   = campo("Stock inicial (ej: 10)");
        TextField minimo  = campo("Stock mínimo (ej: 5)");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10,
                lbl("Nombre *"), nombre, lbl("Descripción"), desc,
                lbl("Precio €*"), precio, lbl("Stock inicial *"), stock,
                lbl("Stock mínimo *"), minimo
        );
        content.setStyle("-fx-padding:16;");
        dialog.getDialogPane().setContent(content);

        // Deshabilitar OK si campos vacíos
        javafx.scene.Node okButton = dialog.getDialogPane()
                .lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        nombre.textProperty().addListener((obs, old, val) ->
                okButton.setDisable(val.trim().isEmpty()));

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    String nombreVal = nombre.getText().trim();
                    String descVal   = desc.getText().trim();
                    double precioVal = Double.parseDouble(
                            precio.getText().trim().replace(",", "."));
                    int stockVal     = Integer.parseInt(stock.getText().trim());
                    int minimoVal    = Integer.parseInt(minimo.getText().trim());

                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("nombre",          nombreVal);
                    body.put("descripcion",     descVal);
                    body.put("precioUnitario",  precioVal);
                    body.put("stockActual",     stockVal);
                    body.put("stockMinimo",     minimoVal);

                    accion(() -> service.crear(body));

                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de formato");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            "Precio, stock y mínimo deben ser números.\n" +
                                    "Ejemplo precio: 12.50  |  Stock: 10");
                    alert.showAndWait();
                }
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
        Label l = new Label(t); l.setStyle("-fx-text-fill:#6b7280; -fx-font-size:12px;"); return l;
    }

    private void datosPrueba() {
        todas.setAll(
                new Pieza(1L, "Filtro de aceite",    "Filtro estándar", 12.50, 2,  5),
                new Pieza(2L, "Pastillas de freno",  "Delanteras",      45.00, 1,  4),
                new Pieza(3L, "Correa distribución", "Universal",       89.00, 0,  2),
                new Pieza(4L, "Aceite motor 5W30",   "1 litro",         8.00,  18, 5),
                new Pieza(5L, "Bujías NGK",          "Set x4",          7.20,  12, 6)
        );
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}