package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.model.Cliente;
import org.example.tfgjavafxpruebas.service.ClientesService;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClientesController extends BaseController implements Initializable {

    @FXML private TableView<Cliente> clientesTable;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String>  colNombre;
    @FXML private TableColumn<Cliente, String>  colEmail;
    @FXML private TableColumn<Cliente, String>  colTelefono;
    @FXML private TableColumn<Cliente, Integer> colPuntos;
    @FXML private TableColumn<Cliente, String>  colGastado;
    @FXML private TableColumn<Cliente, String>  colRegistro;
    @FXML private TextField searchField;

    private final ClientesService service = new ClientesService();
    private final ObservableList<Cliente> todos = FXCollections.observableArrayList();
    private FilteredList<Cliente> filtrados;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();
        filtrados = new FilteredList<>(todos, p -> true);
        clientesTable.setItems(filtrados);
        colId      .setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre  .setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail   .setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colPuntos  .setCellValueFactory(new PropertyValueFactory<>("puntosAcumulados"));
        colGastado .setCellValueFactory(new PropertyValueFactory<>("totalGastado"));
        colRegistro.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));

        searchField.textProperty().addListener((obs, old, val) ->
                filtrados.setPredicate(c -> val == null || val.isEmpty() ||
                        c.getNombre().toLowerCase().contains(val.toLowerCase()) ||
                        c.getEmail().toLowerCase().contains(val.toLowerCase())));

        cargarAsync();
    }

    private void cargarAsync() {
        new Thread(() -> {
            try {
                List<Cliente> list = service.getAll();
                Platform.runLater(() -> todos.setAll(list));
            } catch (Exception e) {
                Platform.runLater(this::datosPrueba);
            }
        }).start();
    }

    private void datosPrueba() {
        todos.setAll(
                new Cliente(1L,"Carlos Martín","carlos@email.com","666111222",340,"1.200€","2025-01-10"),
                new Cliente(2L,"Ana López",    "ana@email.com",   "666333444",820,"2.640€","2024-11-05"),
                new Cliente(3L,"Pedro García", "pedro@email.com", "666555666",120,"380€",  "2026-02-20")
        );
    }
}
