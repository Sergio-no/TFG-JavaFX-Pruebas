package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.MecanicoStats;
import org.example.tfgjavafxpruebas.model.Valoracion;
import org.example.tfgjavafxpruebas.service.EstadisticasService;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class EstadisticasController extends BaseController implements Initializable {

    @FXML private Label metricIngresos;
    @FXML private Label metricReparaciones;
    @FXML private Label metricTicket;
    @FXML private Label metricValoracion;
    @FXML private Label periodoLabel;

    @FXML private StackPane chartMecanicosContainer;
    @FXML private StackPane chartValoracionesContainer;
    @FXML private Label filtroMecanicoLabel;
    @FXML private Label filtroValoracionLabel;

    @FXML private TableView<MecanicoStats> mecanicosTable;
    @FXML private TableColumn<MecanicoStats, String>  colMecNombre;
    @FXML private TableColumn<MecanicoStats, Integer> colMecRep;
    @FXML private TableColumn<MecanicoStats, String>  colMecIngresos;

    @FXML private TableView<Valoracion> valoracionesTable;
    @FXML private TableColumn<Valoracion, String> colValCliente;
    @FXML private TableColumn<Valoracion, String> colValPuntos;
    @FXML private TableColumn<Valoracion, String> colValComentario;

    private final EstadisticasService service = new EstadisticasService();

    private List<MecanicoStats> todosMecanicos = new ArrayList<>();
    private List<Valoracion> todasValoraciones = new ArrayList<>();
    private String mecanicoSeleccionado = null;
    private String valoracionSeleccionada = null;

    private static final String[] COLORES_MECANICOS = {
            "#7ec8e3", "#4ade80", "#d4a72c", "#c084fc",
            "#f87171", "#fb923c", "#38bdf8", "#a78bfa"
    };
    private static final String[] COLORES_VALORACIONES = {
            "#f87171", "#fb923c", "#d4a72c", "#4ade80", "#7ec8e3"
    };

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

        colValCliente   .setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colValPuntos    .setCellValueFactory(new PropertyValueFactory<>("puntuacion"));
        colValComentario.setCellValueFactory(new PropertyValueFactory<>("comentario"));

        cargarAsync();
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                // Una sola llamada HTTP para todo
                JsonNode raw = service.getRaw();
                List<MecanicoStats> mecanicos = service.getMecanicoStatsFromNode(raw);
                List<Valoracion> valoraciones = service.getUltimasValoracionesFromNode(raw);

                String ingresos     = raw.path("ingresosMes").asText("0€");
                String reparaciones = String.valueOf(raw.path("reparacionesMes").asLong(0));
                String ticket       = raw.path("ticketMedio").asText("0€");
                double valMedia     = raw.path("valoracionMedia").asDouble(0);
                String valoracion   = (valMedia > 0)
                        ? String.format("%.1f / 5", valMedia) : "— / 5";

                Platform.runLater(() -> {
                    metricIngresos.setText(ingresos);
                    metricReparaciones.setText(reparaciones);
                    metricTicket.setText(ticket);
                    metricValoracion.setText(valoracion);

                    todosMecanicos = mecanicos;
                    todasValoraciones = valoraciones;

                    mecanicosTable.setItems(FXCollections.observableArrayList(mecanicos));
                    valoracionesTable.setItems(FXCollections.observableArrayList(valoraciones));

                    crearGraficoMecanicos(mecanicos);
                    crearGraficoValoraciones(valoraciones);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    // ══════════════════════════════════════════════════════════
    // GRÁFICO DONUT: Rendimiento por mecánico
    // ══════════════════════════════════════════════════════════
    private void crearGraficoMecanicos(List<MecanicoStats> mecanicos) {
        chartMecanicosContainer.getChildren().clear();

        if (mecanicos.isEmpty()) {
            mostrarMensajeVacio(chartMecanicosContainer, "No hay mecánicos registrados");
            return;
        }

        int totalRep = mecanicos.stream().mapToInt(MecanicoStats::getReparaciones).sum();

        // Si todos tienen 0 reparaciones, mostrar gráfico especial
        if (totalRep == 0) {
            mostrarDonutVacio(chartMecanicosContainer, "0\nreparaciones",
                    mecanicos.stream().map(MecanicoStats::getNombre).toList());
            return;
        }

        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        for (MecanicoStats m : mecanicos) {
            if (m.getReparaciones() > 0) {
                double porcentaje = m.getReparaciones() * 100.0 / totalRep;
                String label = m.getNombre() + " (" + String.format("%.1f", porcentaje) + "%)";
                datos.add(new PieChart.Data(label, m.getReparaciones()));
            }
        }

        PieChart chart = crearPieChart(datos);

        Circle centroDonut = crearCentroDonut();
        Label centralLabel = crearCentralLabel(totalRep + "\nreparaciones");

        StackPane donutPane = new StackPane(chart, centroDonut, centralLabel);
        donutPane.setAlignment(Pos.CENTER);
        chartMecanicosContainer.getChildren().add(donutPane);

        // Aplicar colores y handlers de click
        Platform.runLater(() -> {
            // Filtrar mecánicos con reparaciones > 0 (los que están en el gráfico)
            List<MecanicoStats> mecanicosEnGrafico = mecanicos.stream()
                    .filter(m -> m.getReparaciones() > 0).toList();

            for (int i = 0; i < datos.size(); i++) {
                PieChart.Data d = datos.get(i);
                String color = COLORES_MECANICOS[i % COLORES_MECANICOS.length];
                final String nombreMecanico = mecanicosEnGrafico.get(i).getNombre();

                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + color + ";");

                    d.getNode().setOnMouseClicked(event -> {
                        if (nombreMecanico.equals(mecanicoSeleccionado)) {
                            mecanicoSeleccionado = null;
                            filtroMecanicoLabel.setText("");
                            mecanicosTable.setItems(
                                    FXCollections.observableArrayList(todosMecanicos));
                            resetearOpacidad(chart);
                        } else {
                            mecanicoSeleccionado = nombreMecanico;
                            filtroMecanicoLabel.setText("Filtrado: " + nombreMecanico);
                            mecanicosTable.setItems(FXCollections.observableArrayList(
                                    todosMecanicos.stream()
                                            .filter(m -> m.getNombre().equals(nombreMecanico))
                                            .collect(Collectors.toList())
                            ));
                            resaltarSlice(chart, datos.indexOf(d));
                        }
                    });

                    configurarHover(d, color, () -> mecanicoSeleccionado,
                            () -> nombreMecanico);
                }
            }
            estilizarLeyenda(chart);
        });
    }

    // ══════════════════════════════════════════════════════════
    // GRÁFICO DONUT: Distribución de valoraciones
    // ══════════════════════════════════════════════════════════
    private void crearGraficoValoraciones(List<Valoracion> valoraciones) {
        chartValoracionesContainer.getChildren().clear();

        if (valoraciones.isEmpty()) {
            mostrarMensajeVacio(chartValoracionesContainer, "Sin valoraciones aún");
            return;
        }

        // Contar por puntuación
        Map<Integer, Long> conteo = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) conteo.put(i, 0L);

        for (Valoracion v : valoraciones) {
            int estrellas = contarEstrellas(v.getPuntuacion());
            if (estrellas >= 1 && estrellas <= 5) {
                conteo.merge(estrellas, 1L, Long::sum);
            }
        }

        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        List<Integer> estrellasEnGrafico = new ArrayList<>();
        long total = 0;

        for (Map.Entry<Integer, Long> entry : conteo.entrySet()) {
            if (entry.getValue() > 0) {
                total += entry.getValue();
                double porcentaje = 0; // se calcula después
                estrellasEnGrafico.add(entry.getKey());
                datos.add(new PieChart.Data(
                        entry.getKey() + " ★",
                        entry.getValue()));
            }
        }

        if (datos.isEmpty()) {
            mostrarMensajeVacio(chartValoracionesContainer, "Sin valoraciones aún");
            return;
        }

        // Actualizar labels con porcentajes
        for (PieChart.Data d : datos) {
            double porcentaje = d.getPieValue() * 100.0 / total;
            d.setName(d.getName() + " (" + String.format("%.1f", porcentaje) + "%)");
        }

        PieChart chart = crearPieChart(datos);
        Circle centroDonut = crearCentroDonut();
        Label centralLabel = crearCentralLabel(total + "\nreseñas");

        StackPane donutPane = new StackPane(chart, centroDonut, centralLabel);
        donutPane.setAlignment(Pos.CENTER);
        chartValoracionesContainer.getChildren().add(donutPane);

        Platform.runLater(() -> {
            for (int i = 0; i < datos.size(); i++) {
                PieChart.Data d = datos.get(i);
                int estrellas = estrellasEnGrafico.get(i);
                String color = COLORES_VALORACIONES[
                        (estrellas - 1) % COLORES_VALORACIONES.length];
                final String estrellaKey = estrellas + " ★";
                final int numEstrellas = estrellas;

                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + color + ";");

                    d.getNode().setOnMouseClicked(event -> {
                        if (estrellaKey.equals(valoracionSeleccionada)) {
                            valoracionSeleccionada = null;
                            filtroValoracionLabel.setText("");
                            valoracionesTable.setItems(
                                    FXCollections.observableArrayList(todasValoraciones));
                            resetearOpacidad(chart);
                        } else {
                            valoracionSeleccionada = estrellaKey;
                            filtroValoracionLabel.setText(
                                    "Filtrado: " + numEstrellas + " estrellas");
                            valoracionesTable.setItems(FXCollections.observableArrayList(
                                    todasValoraciones.stream()
                                            .filter(v -> contarEstrellas(
                                                    v.getPuntuacion()) == numEstrellas)
                                            .collect(Collectors.toList())
                            ));
                            resaltarSlice(chart, datos.indexOf(d));
                        }
                    });

                    configurarHover(d, color, () -> valoracionSeleccionada,
                            () -> estrellaKey);
                }
            }
            estilizarLeyenda(chart);
        });
    }

    // ══════════════════════════════════════════════════════════
    // Helpers comunes para gráficos
    // ══════════════════════════════════════════════════════════

    private PieChart crearPieChart(ObservableList<PieChart.Data> datos) {
        PieChart chart = new PieChart(datos);
        chart.setLabelsVisible(false);
        chart.setLegendVisible(true);
        chart.setAnimated(true);
        chart.setStartAngle(90);
        chart.setPrefSize(400, 260);
        chart.setMaxSize(400, 260);
        return chart;
    }

    private Circle crearCentroDonut() {
        Circle c = new Circle(60);
        c.setFill(Color.web("#1a1d23"));
        c.setStroke(Color.web("#2e333d"));
        c.setStrokeWidth(1);
        return c;
    }

    private Label crearCentralLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 13px; " +
                "-fx-text-alignment: center;");
        l.setAlignment(Pos.CENTER);
        return l;
    }

    private void mostrarMensajeVacio(StackPane container, String mensaje) {
        Label l = new Label(mensaje);
        l.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");
        container.getChildren().clear();
        container.getChildren().add(l);
    }

    /**
     * Muestra un donut "vacío" (anillo gris) con nombres en la leyenda
     * cuando todos los mecánicos tienen 0 reparaciones.
     */
    private void mostrarDonutVacio(StackPane container, String textoCenter,
                                   List<String> nombres) {
        // Crear un gráfico con valores ficticios iguales para mostrar anillo uniforme
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        for (String nombre : nombres) {
            datos.add(new PieChart.Data(nombre + " (0%)", 1));
        }

        PieChart chart = crearPieChart(datos);
        Circle centro = crearCentroDonut();
        Label centralLabel = crearCentralLabel(textoCenter);

        StackPane donutPane = new StackPane(chart, centro, centralLabel);
        donutPane.setAlignment(Pos.CENTER);
        container.getChildren().add(donutPane);

        Platform.runLater(() -> {
            for (int i = 0; i < datos.size(); i++) {
                PieChart.Data d = datos.get(i);
                String color = COLORES_MECANICOS[i % COLORES_MECANICOS.length];
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + color + "; -fx-opacity: 0.3;");
                }
            }
            estilizarLeyenda(chart);
        });
    }

    private void resaltarSlice(PieChart chart, int selectedIndex) {
        ObservableList<PieChart.Data> datos = chart.getData();
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getNode() != null) {
                String color = extraerColor(datos.get(i).getNode().getStyle());
                double opacity = (i == selectedIndex) ? 1.0 : 0.4;
                datos.get(i).getNode().setStyle(
                        "-fx-pie-color: " + color + "; -fx-opacity: " + opacity + ";");
            }
        }
    }

    private void resetearOpacidad(PieChart chart) {
        for (PieChart.Data d : chart.getData()) {
            if (d.getNode() != null) {
                String color = extraerColor(d.getNode().getStyle());
                d.getNode().setStyle("-fx-pie-color: " + color + "; -fx-opacity: 1.0;");
            }
        }
    }

    private void configurarHover(PieChart.Data d, String color,
                                 java.util.function.Supplier<String> getSeleccionado,
                                 java.util.function.Supplier<String> getKey) {
        d.getNode().setOnMouseEntered(e ->
                d.getNode().setStyle("-fx-pie-color: " + color +
                        "; -fx-opacity: 0.85; -fx-cursor: hand;"));
        d.getNode().setOnMouseExited(e -> {
            String sel = getSeleccionado.get();
            if (sel != null && !getKey.get().equals(sel)) {
                d.getNode().setStyle("-fx-pie-color: " + color + "; -fx-opacity: 0.4;");
            } else {
                d.getNode().setStyle("-fx-pie-color: " + color + "; -fx-opacity: 1.0;");
            }
        });
    }

    private void estilizarLeyenda(PieChart chart) {
        // Texto de la leyenda
        chart.lookupAll(".chart-legend-item").forEach(node ->
                node.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 10px;"));

        // Fondo transparente
        var legend = chart.lookup(".chart-legend");
        if (legend != null) {
            legend.setStyle("-fx-background-color: transparent;");
        }

        // Sincronizar el color del símbolo de leyenda con el del slice
        ObservableList<PieChart.Data> datos = chart.getData();
        for (int i = 0; i < datos.size(); i++) {
            String color = extraerColor(datos.get(i).getNode().getStyle());
            javafx.scene.Node symbol = chart.lookup(
                    ".default-color" + i + ".chart-legend-item-symbol");
            if (symbol != null) {
                symbol.setStyle("-fx-background-color: " + color + ";");
            }
        }
    }

    private String extraerColor(String style) {
        int idx = style.indexOf("-fx-pie-color:");
        if (idx >= 0) {
            String sub = style.substring(idx + 14).trim();
            int end = sub.indexOf(";");
            if (end > 0) return sub.substring(0, end).trim();
            return sub.trim();
        }
        return "#7ec8e3";
    }

    private int contarEstrellas(String puntuacion) {
        if (puntuacion == null) return 0;
        int count = 0;
        for (char c : puntuacion.toCharArray()) {
            if (c == '★') count++;
        }
        return count;
    }

    // ── Datos de prueba (fallback si no hay conexión) ──
    private void datosPrueba() {
        metricIngresos.setText("8.420€");
        metricReparaciones.setText("34");
        metricTicket.setText("247€");
        metricValoracion.setText("4.7 / 5");

        todosMecanicos = List.of(
                new MecanicoStats("Juan Ramos",     14, "3.200€"),
                new MecanicoStats("Miguel Torres",  12, "2.880€"),
                new MecanicoStats("Luis Fernández",  8, "2.340€")
        );
        todasValoraciones = List.of(
                new Valoracion("Ana López",    5, "Excelente servicio"),
                new Valoracion("Carlos Martín",4, "Muy buen trato"),
                new Valoracion("Pedro García", 5, "Rápidos y profesionales"),
                new Valoracion("Laura Sánchez",3, "Normal, mejorable"),
                new Valoracion("Diego Ruiz",   4, "Buen trabajo"),
                new Valoracion("María Gómez",  5, "Perfecto")
        );

        mecanicosTable.setItems(FXCollections.observableArrayList(todosMecanicos));
        valoracionesTable.setItems(FXCollections.observableArrayList(todasValoraciones));

        crearGraficoMecanicos(todosMecanicos);
        crearGraficoValoraciones(todasValoraciones);
    }
}