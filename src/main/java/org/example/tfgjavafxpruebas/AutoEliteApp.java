package org.example.tfgjavafxpruebas;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
public class AutoEliteApp extends Application {
    public static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        navigateTo("login");
    }

    public static void navigateTo(String fxmlName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    AutoEliteApp.class.getResource("/org/example/tfgjavafxpruebas/" + fxmlName + ".fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    AutoEliteApp.class.getResource("/org/example/tfgjavafxpruebas/styles/autoelite.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.setTitle("AutoElite");
            primaryStage.setMinWidth(1100);
            primaryStage.setMinHeight(700);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
