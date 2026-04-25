package org.example.tfgjavafxpruebas.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.example.tfgjavafxpruebas.sesion.UserSesion;

import java.net.URL;
import java.util.ResourceBundle;

public class SidebarController extends BaseController implements Initializable {

    @FXML private Button navEmpleados;
    @FXML private Button navEstadisticas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initUserLabel();

        boolean esJefe = UserSesion.getInstance().isJefe();

        // Empleados: solo visible para JEFE
        if (navEmpleados != null) {
            navEmpleados.setVisible(esJefe);
            navEmpleados.setManaged(esJefe);
        }

        // Estadísticas: solo visible para JEFE
        if (navEstadisticas != null) {
            navEstadisticas.setVisible(esJefe);
            navEstadisticas.setManaged(esJefe);
        }
    }
}
