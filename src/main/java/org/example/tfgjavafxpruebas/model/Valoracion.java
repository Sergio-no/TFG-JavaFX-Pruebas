package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.SimpleStringProperty;

public class Valoracion {
    private final SimpleStringProperty cliente    = new SimpleStringProperty();
    private final SimpleStringProperty puntuacion = new SimpleStringProperty();
    private final SimpleStringProperty comentario = new SimpleStringProperty();

    public Valoracion(String cliente, int puntuacion, String comentario) {
        this.cliente.set(cliente);
        this.puntuacion.set("★".repeat(puntuacion) + "☆".repeat(5 - puntuacion));
        this.comentario.set(comentario);
    }

    public String getCliente()    { return cliente.get(); }
    public String getPuntuacion() { return puntuacion.get(); }
    public String getComentario() { return comentario.get(); }

    public SimpleStringProperty clienteProperty()    { return cliente; }
    public SimpleStringProperty puntuacionProperty() { return puntuacion; }
    public SimpleStringProperty comentarioProperty() { return comentario; }
}
