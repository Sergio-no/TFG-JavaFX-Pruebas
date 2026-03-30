module org.example.tfgjavafxpruebas {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens org.example.tfgjavafxpruebas.controller to javafx.fxml;

    opens org.example.tfgjavafxpruebas to javafx.fxml;
    exports org.example.tfgjavafxpruebas;
}