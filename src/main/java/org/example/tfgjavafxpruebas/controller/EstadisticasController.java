package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.MecanicoStats;
import org.example.tfgjavafxpruebas.model.Valoracion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class EstadisticasController extends BaseController implements Initializable {

    @FXML private Label metricIngresos;
    @FXML private Label metricReparaciones;
    @FXML private Label metricTicket;
    @FXML private Label metricValoracion;
    @FXML private Label periodoLabel;

    @FXML private TableView<MecanicoStats> mecanicosTable;
    @FXML private TableColumn<MecanicoStats, String>  colMecNombre;
    @FXML private TableColumn<MecanicoStats, Integer> colMecRep;
    @FXML private TableColumn<MecanicoStats, String>  colMecIngresos;

    @FXML private TableView<Valoracion> valoracionesTable;
    @FXML private TableColumn<Valoracion, String> colValCliente;
    @FXML private TableColumn<Valoracion, String> colValPuntos;
    @FXML private TableColumn<Valoracion, String> colValComentario;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();

        LocalDate hoy = LocalDate.now();
        periodoLabel.setText(
                hoy.getMonth().getDisplayName(TextStyle.FULL, new Locale("es"))
                        + " " + hoy.getYear());

        colMecNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colMecRep     .setCellValueFactory(new PropertyValueFactory<>("reparaciones"));
        colMecIngresos.setCellValueFactory(new PropertyValueFactory<>("ingresos"));

        colValCliente  .setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colValPuntos   .setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValComentario.setCellValueFactory(new PropertyValueFactory<>("comentario"));

        // Datos de prueba — sustituir por llamada al backend
        cargarDatosPrueba();
    }

    private void cargarDatosPrueba() {
        metricIngresos.setText("8.420€");
        metricReparaciones.setText("34");
        metricTicket.setText("247€");
        metricValoracion.setText("4.7 / 5");

        mecanicosTable.setItems(FXCollections.observableArrayList(
                new MecanicoStats("Juan Ramos",      14, "3.200€"),
                new MecanicoStats("Miguel Torres",   12, "2.880€"),
                new MecanicoStats("Luis Fernández",   8, "2.340€")
        ));

        valoracionesTable.setItems(FXCollections.observableArrayList(
                new Valoracion("Ana López",    5, "Excelente servicio"),
                new Valoracion("Carlos Martín",4, "Muy buen trato"),
                new Valoracion("Pedro García", 5, "Rápidos y profesionales")
        ));
    }
}
