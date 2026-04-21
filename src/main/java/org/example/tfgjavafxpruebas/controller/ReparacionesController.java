package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Pieza;
import org.example.tfgjavafxpruebas.model.Reparacion;
import org.example.tfgjavafxpruebas.service.InventarioService;
import org.example.tfgjavafxpruebas.service.ReparacionesService;
import org.example.tfgjavafxpruebas.service.VehiculosService;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.tfgjavafxpruebas.util.ConfirmDialog;

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
    private final VehiculosService vehiculosService = new VehiculosService();
    private final InventarioService inventarioService = new InventarioService();
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
                    if (ConfirmDialog.ask("Terminar reparación",
                            "¿Marcar la reparación de " + r.getVehiculo() + " como terminada?")) {
                        accion(() -> service.cambiarEstado(r.getId(), "TERMINADA"));
                    }
                });
                confirmar.setOnAction(e -> {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    if (ConfirmDialog.ask("Confirmar reparación",
                            "¿Confirmar y cerrar la reparación de " + r.getVehiculo()
                                    + "?\nSe generará una factura automáticamente.")) {
                        accion(() -> service.cambiarEstado(r.getId(), "CONFIRMADA"));
                    }
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

    // ══════════════════════════════════════════════════════════
    // NUEVO: Diálogo de nueva reparación con matrícula, coste y piezas
    // ══════════════════════════════════════════════════════════

    @FXML private void abrirNuevaReparacion() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nueva reparación");
        dialog.getDialogPane().setStyle(
                "-fx-background-color:#1a1d23; -fx-min-width:520;");
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Matrícula con botón buscar ──
        TextField matriculaField = campo("Ej: 1234ABC");
        Button buscarBtn = new Button("Buscar");
        buscarBtn.getStyleClass().add("btn-primary");
        buscarBtn.setStyle("-fx-padding:8 16; -fx-font-size:12px;");
        Label vehiculoInfo = new Label("Introduce una matrícula y pulsa Buscar");
        vehiculoInfo.setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;");
        vehiculoInfo.setWrapText(true);
        HBox matriculaBox = new HBox(8, matriculaField, buscarBtn);
        HBox.setHgrow(matriculaField, Priority.ALWAYS);

        // Variable para almacenar el vehículo encontrado
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

        // ── Mecánico combo ──
        ComboBox<org.example.tfgjavafxpruebas.service.MecanicosService.MecanicoItem>
                mecanicoCombo = new ComboBox<>();
        mecanicoCombo.getStyleClass().add("input-field");
        mecanicoCombo.setMaxWidth(Double.MAX_VALUE);
        mecanicoCombo.setPromptText("Selecciona mecánico...");
        new Thread(() -> {
            try {
                var lista = new org.example.tfgjavafxpruebas.service.MecanicosService()
                        .getActivos();
                Platform.runLater(() -> mecanicoCombo.getItems().setAll(lista));
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        // ── Coste de mano de obra ──
        TextField costeField = campo("0.00");

        // ── Cita (opcional) ──
        TextField citaField = campo("ID Cita (opcional)");

        // ── Selección de piezas ──
        ComboBox<PiezaItem> piezaCombo = new ComboBox<>();
        piezaCombo.getStyleClass().add("input-field");
        piezaCombo.setMaxWidth(Double.MAX_VALUE);
        piezaCombo.setPromptText("Selecciona pieza...");

        Spinner<Integer> cantidadSpinner = new Spinner<>(1, 100, 1);
        cantidadSpinner.setPrefWidth(80);
        cantidadSpinner.setStyle("-fx-background-color:#12151a;");

        Button addPiezaBtn = new Button("Añadir");
        addPiezaBtn.getStyleClass().add("btn-primary");
        addPiezaBtn.setStyle("-fx-padding:6 12; -fx-font-size:11px;");

        HBox piezaRow = new HBox(8, piezaCombo, cantidadSpinner, addPiezaBtn);
        HBox.setHgrow(piezaCombo, Priority.ALWAYS);

        // Lista de piezas seleccionadas
        VBox piezasListContainer = new VBox(4);
        piezasListContainer.setStyle("-fx-padding:4 0;");
        List<PiezaSeleccionada> piezasSeleccionadas = new ArrayList<>();

        // Cargar piezas disponibles
        new Thread(() -> {
            try {
                List<Pieza> piezas = inventarioService.getAll();
                List<PiezaItem> items = piezas.stream()
                        .filter(p -> p.getStockActual() > 0)
                        .map(p -> new PiezaItem(p.getId(), p.getNombre(),
                                p.getPrecioUnitario(), p.getStockActual()))
                        .toList();
                Platform.runLater(() -> piezaCombo.getItems().setAll(items));
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        addPiezaBtn.setOnAction(e -> {
            PiezaItem selected = piezaCombo.getValue();
            if (selected == null) return;
            int cantidad = cantidadSpinner.getValue();
            if (cantidad > selected.stock) {
                ConfirmDialog.error("Stock insuficiente",
                        "Solo hay " + selected.stock + " unidades de "
                                + selected.nombre);
                return;
            }
            // Check if already added
            boolean yaExiste = piezasSeleccionadas.stream()
                    .anyMatch(ps -> ps.piezaId.equals(selected.id));
            if (yaExiste) {
                ConfirmDialog.error("Pieza duplicada",
                        "Ya has añadido esa pieza. Elimínala primero.");
                return;
            }
            PiezaSeleccionada ps = new PiezaSeleccionada(
                    selected.id, selected.nombre, cantidad, selected.precio);
            piezasSeleccionadas.add(ps);
            actualizarListaPiezas(piezasListContainer, piezasSeleccionadas);
        });

        // ── Layout del diálogo ──
        VBox content = new VBox(10,
                lbl("Matrícula del vehículo *"), matriculaBox, vehiculoInfo,
                new Separator(),
                lbl("Mecánico *"),               mecanicoCombo,
                lbl("Coste mano de obra (€)"),   costeField,
                lbl("ID Cita (opcional)"),        citaField,
                new Separator(),
                lbl("Piezas a utilizar"),         piezaRow,
                piezasListContainer
        );
        content.setStyle("-fx-padding:16;");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:#1a1d23; -fx-background-color:#1a1d23;");
        scroll.setPrefHeight(500);
        dialog.getDialogPane().setContent(scroll);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                if (vehiculoEncontrado[0] == null) {
                    ConfirmDialog.error("Vehículo no encontrado",
                            "Busca un vehículo válido por matrícula.");
                    return null;
                }
                if (mecanicoCombo.getValue() == null) {
                    ConfirmDialog.error("Mecánico requerido",
                            "Selecciona un mecánico.");
                    return null;
                }
                try {
                    double coste = 0;
                    String costeText = costeField.getText().trim()
                            .replace(",", ".");
                    if (!costeText.isEmpty()) {
                        coste = Double.parseDouble(costeText);
                    }

                    // 1. Crear la reparación
                    Map<String, Object> body = new HashMap<>();
                    body.put("vehiculoId", vehiculoEncontrado[0].getId());
                    body.put("mecanicoId", mecanicoCombo.getValue().getId());
                    if (!citaField.getText().isBlank())
                        body.put("citaId",
                                Long.parseLong(citaField.getText().trim()));

                    // Calcular coste total: mano de obra + piezas
                    double costePiezas = piezasSeleccionadas.stream()
                            .mapToDouble(ps -> ps.precio * ps.cantidad)
                            .sum();
                    body.put("costeInicial", coste + costePiezas);

                    // Copiar lista para el hilo
                    List<PiezaSeleccionada> piezasCopy =
                            new ArrayList<>(piezasSeleccionadas);

                    accion(() -> {
                        // Crear reparación
                        Long reparacionId = service.crearYObtenerId(body);

                        // Añadir piezas una a una
                        for (PiezaSeleccionada ps : piezasCopy) {
                            service.addPieza(reparacionId,
                                    ps.piezaId, ps.cantidad);
                        }
                    });

                } catch (NumberFormatException ex) {
                    ConfirmDialog.error("Error de formato",
                            "El coste y los IDs deben ser números válidos.");
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void actualizarListaPiezas(VBox container,
                                       List<PiezaSeleccionada> piezas) {
        container.getChildren().clear();
        for (PiezaSeleccionada ps : piezas) {
            Label info = new Label(ps.cantidad + "x " + ps.nombre
                    + "  (" + String.format("%.2f€", ps.precio * ps.cantidad)
                    + ")");
            info.setStyle("-fx-text-fill:#e2e8f0; -fx-font-size:12px;");

            Button quitar = new Button("✗");
            quitar.setStyle("-fx-background-color:transparent; "
                    + "-fx-text-fill:#f87171; -fx-cursor:hand; "
                    + "-fx-font-size:12px; -fx-padding:0 4;");
            quitar.setOnAction(e -> {
                piezas.remove(ps);
                actualizarListaPiezas(container, piezas);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(8, info, spacer, quitar);
            row.setStyle("-fx-background-color:#12151a; -fx-padding:6 10; "
                    + "-fx-background-radius:4;");
            container.getChildren().add(row);
        }
    }

    // ── Clases auxiliares para el diálogo ──

    private static class PiezaItem {
        final Long id;
        final String nombre;
        final double precio;
        final int stock;
        PiezaItem(Long id, String nombre, double precio, int stock) {
            this.id = id; this.nombre = nombre;
            this.precio = precio; this.stock = stock;
        }
        @Override public String toString() {
            return nombre + " (" + String.format("%.2f€", precio)
                    + " · stock: " + stock + ")";
        }
    }

    private static class PiezaSeleccionada {
        final Long piezaId;
        final String nombre;
        final int cantidad;
        final double precio;
        PiezaSeleccionada(Long piezaId, String nombre,
                          int cantidad, double precio) {
            this.piezaId = piezaId; this.nombre = nombre;
            this.cantidad = cantidad; this.precio = precio;
        }
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
                new Reparacion(1L, "Seat Ibiza · Carlos M.", "Juan Ramos",
                        "2026-03-27","", "EN_PROCESO","320€"),
                new Reparacion(2L, "Ford Focus · Ana L.", "Miguel Torres",
                        "2026-03-26","", "EN_PROCESO","480€"),
                new Reparacion(3L, "Renault Megane", "Juan Ramos",
                        "2026-03-25","2026-03-27","TERMINADA", "180€")
        );
    }

    @FunctionalInterface interface RunnableEx { void run() throws Exception; }
}
