package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class BaseController {

    @FXML protected Label userEmailLabel;

    @FXML protected void goToDashboard()    { AutoEliteApp.navigateTo("dashboard"); }
    @FXML protected void goToCitas()        { AutoEliteApp.navigateTo("citas"); }
    @FXML protected void goToReparaciones() { AutoEliteApp.navigateTo("reparaciones"); }
    @FXML protected void goToInventario()   { AutoEliteApp.navigateTo("inventario"); }
    @FXML protected void goToFacturas()     { AutoEliteApp.navigateTo("facturas"); }
    @FXML protected void goToClientes()     { AutoEliteApp.navigateTo("clientes"); }
    @FXML protected void goToEstadisticas() { AutoEliteApp.navigateTo("estadisticas"); }

    @FXML
    protected void handleLogout() {
        if (org.example.tfgjavafxpruebas.util.ConfirmDialog.ask(
                "Cerrar sesión", "¿Seguro que quieres cerrar sesión?")) {
            UserSesion.getInstance().clear();
            AutoEliteApp.navigateTo("login");
        }
    }

    protected void initUserLabel() {
        if (userEmailLabel != null)
            userEmailLabel.setText(UserSesion.getInstance().getEmail());
    }
}
