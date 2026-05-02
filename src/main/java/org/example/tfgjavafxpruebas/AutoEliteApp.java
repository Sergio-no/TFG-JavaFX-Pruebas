package org.example.tfgjavafxpruebas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class AutoEliteApp extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        // Icono de la ventana
        try {
            stage.getIcons().add(new Image(
                    AutoEliteApp.class.getResourceAsStream(
                            "/org/example/tfgjavafxpruebas/images/logo.png")));
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        navigateTo("login");
    }

    public static void navigateTo(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AutoEliteApp.class.getResource("/org/example/tfgjavafxpruebas/" + fxmlName + ".fxml")
            );
            Parent root = loader.load();

            if (primaryStage.getScene() == null) {
                // Primera vez: crear la Scene
                Scene scene = new Scene(root);
                scene.getStylesheets().add(
                        AutoEliteApp.class.getResource(
                                "/org/example/tfgjavafxpruebas/styles/autoelite.css"
                        ).toExternalForm()
                );
                primaryStage.setScene(scene);
                primaryStage.setTitle("AutoElite");
                primaryStage.setMinWidth(1100);
                primaryStage.setMinHeight(700);
                primaryStage.show();
            } else {
                // Navegaciones siguientes: solo reemplazar el root.
                // La Scene y el Stage no cambian → maximizado se conserva.
                primaryStage.getScene().setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}