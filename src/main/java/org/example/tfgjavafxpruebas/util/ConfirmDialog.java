package org.example.tfgjavafxpruebas.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ConfirmDialog {

    // Muestra un popup sí/no y devuelve true si el usuario acepta.
    public static boolean ask(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1d23;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #e2e8f0;");
        Optional<ButtonType> r = alert.showAndWait();
        return r.isPresent() && r.get() == ButtonType.OK;
    }

    // Muestra un popup de error informativo.
    public static void error(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1d23;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #e2e8f0;");
        alert.showAndWait();
    }

    // Muestra un popup informativo de éxito.
    public static void info(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle("-fx-background-color: #1a1d23;");
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #e2e8f0;");
        alert.showAndWait();
    }
}
