package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.model.AlertaStock;
import org.example.tfgjavafxpruebas.model.CitaResumen;
import org.example.tfgjavafxpruebas.model.ReparacionResumen;
import org.example.tfgjavafxpruebas.service.DashboardService;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // ── Métricas
    @FXML private Label metricCitas;
    @FXML private Label metricCitasSub;
    @FXML private Label metricRep;
    @FXML private Label metricRepSub;
    @FXML private Label metricFacturacion;
    @FXML private Label metricFacturacionSub;
    @FXML private Label metricAlertas;
    @FXML private Label metricAlertasSub;

    // ── Tabla citas
    @FXML private TableView<CitaResumen> citasTable;
    @FXML private TableColumn<CitaResumen, String> colCitaCliente;
    @FXML private TableColumn<CitaResumen, String> colCitaHora;
    @FXML private TableColumn<CitaResumen, String> colCitaTipo;
    @FXML private TableColumn<CitaResumen, String> colCitaEstado;

    // ── Tabla reparaciones
    @FXML private TableView<ReparacionResumen> reparacionesTable;
    @FXML private TableColumn<ReparacionResumen, String> colRepVehiculo;
    @FXML private TableColumn<ReparacionResumen, String> colRepMecanico;
    @FXML private TableColumn<ReparacionResumen, String> colRepInicio;
    @FXML private TableColumn<ReparacionResumen, String> colRepEstado;
    @FXML private TableColumn<ReparacionResumen, String> colRepCoste;

    // ── Alertas
    @FXML private VBox alertasContainer;

    // ── Topbar / sidebar
    @FXML private Label fechaLabel;
    @FXML private Label rolBadge;
    @FXML private Label userEmailLabel;

    private final DashboardService service = new DashboardService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        configurarTopbar();
        cargarDatosAsync();
    }

    private void configurarColumnas() {
        colCitaCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colCitaHora   .setCellValueFactory(new PropertyValueFactory<>("hora"));
        colCitaTipo   .setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCitaEstado .setCellValueFactory(new PropertyValueFactory<>("estado"));

        colRepVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colRepMecanico.setCellValueFactory(new PropertyValueFactory<>("mecanico"));
        colRepInicio  .setCellValueFactory(new PropertyValueFactory<>("inicio"));
        colRepEstado  .setCellValueFactory(new PropertyValueFactory<>("estado"));
        colRepCoste   .setCellValueFactory(new PropertyValueFactory<>("coste"));
    }

    private void configurarTopbar() {
        LocalDate hoy = LocalDate.now();
        String diaSemana = hoy.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es"));
        String fecha = diaSemana + ", " +
                hoy.format(DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es")));
        fechaLabel.setText(fecha);

        UserSesion session = UserSesion.getInstance();
        userEmailLabel.setText(session.getEmail());
        if (session.getRol() != null) rolBadge.setText(session.getRol());
    }

    private void cargarDatosAsync() {
        new Thread(() -> {
            try {
                List<CitaResumen> citas           = service.getCitasHoy();
                List<ReparacionResumen> reps       = service.getReparacionesActivas();
                List<AlertaStock> alertas          = service.getAlertasStock();

                Platform.runLater(() -> {
                    // Métricas
                    metricCitas.setText(String.valueOf(citas.size()));
                    metricCitasSub.setText(
                            citas.stream().filter(c -> "PENDIENTE".equals(c.getEstado())).count()
                                    + " pendientes");

                    metricRep.setText(String.valueOf(reps.size()));
                    metricRepSub.setText("en proceso");

                    metricAlertas.setText(String.valueOf(alertas.size()));
                    metricAlertasSub.setText("piezas bajo mínimo");

                    // Facturación — endpoint separado, de momento placeholder
                    metricFacturacion.setText("—");
                    metricFacturacionSub.setText("cargando...");

                    // Tablas
                    citasTable.setItems(FXCollections.observableArrayList(citas));
                    reparacionesTable.setItems(FXCollections.observableArrayList(reps));

                    // Alertas de stock
                    alertasContainer.getChildren().clear();
                    for (AlertaStock alerta : alertas) {
                        alertasContainer.getChildren().add(crearFilaAlerta(alerta));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> usarDatosDePrueba());
            }
        }).start();
    }

    // Fila visual de alerta de stock
    private HBox crearFilaAlerta(AlertaStock alerta) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #2d1a1a; -fx-padding: 8 10; " +
                "-fx-background-radius: 6;");

        Label nombre = new Label(alerta.getNombre());
        nombre.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stock = new Label(alerta.getStockActual() + " uds (mín. " +
                alerta.getStockMinimo() + ")");
        stock.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        row.getChildren().addAll(nombre, spacer, stock);
        return row;
    }

    // Datos de prueba mientras el backend no está listo
    private void usarDatosDePrueba() {
        metricCitas.setText("8");
        metricCitasSub.setText("3 pendientes");
        metricRep.setText("5");
        metricRepSub.setText("en proceso");
        metricFacturacion.setText("1.240€");
        metricFacturacionSub.setText("4 facturas hoy");
        metricAlertas.setText("3");
        metricAlertasSub.setText("piezas bajo mínimo");

        citasTable.setItems(FXCollections.observableArrayList(
                new CitaResumen("Carlos Martín",  "09:00", "Revisión",   "CONFIRMADA"),
                new CitaResumen("Ana López",      "10:30", "Frenos",     "PENDIENTE"),
                new CitaResumen("Taller Norte",   "12:00", "Diagnóstico","CONFIRMADA"),
                new CitaResumen("Pedro García",   "16:00", "Aceite",     "PENDIENTE")
        ));

        reparacionesTable.setItems(FXCollections.observableArrayList(
                new ReparacionResumen("Seat Ibiza · Carlos M.", "Juan Ramos",  "27/03", "EN_PROCESO", "320€"),
                new ReparacionResumen("Ford Focus · Ana L.",    "Miguel Torres","26/03","EN_PROCESO", "480€"),
                new ReparacionResumen("Renault Megane",         "Juan Ramos",  "25/03", "TERMINADA",  "180€")
        ));

        alertasContainer.getChildren().clear();
        List.of(
                new AlertaStock("Filtro de aceite",     2, 5),
                new AlertaStock("Pastillas de freno",   1, 4),
                new AlertaStock("Correa distribución",  0, 2)
        ).forEach(a -> alertasContainer.getChildren().add(crearFilaAlerta(a)));
    }

    // ── Navegación sidebar ────────────────────────────────
    @FXML private void goToDashboard()     { AutoEliteApp.navigateTo("dashboard"); }
    @FXML private void goToCitas()         { AutoEliteApp.navigateTo("citas"); }
    @FXML private void goToReparaciones()  { AutoEliteApp.navigateTo("reparaciones"); }
    @FXML private void goToInventario()    { AutoEliteApp.navigateTo("inventario"); }
    @FXML private void goToFacturas()      { AutoEliteApp.navigateTo("facturas"); }
    @FXML private void goToClientes()      { AutoEliteApp.navigateTo("clientes"); }
    @FXML private void goToEstadisticas()  { AutoEliteApp.navigateTo("estadisticas"); }

    @FXML
    private void handleLogout() {
        UserSesion.getInstance().clear();
        AutoEliteApp.navigateTo("login");
    }
}
