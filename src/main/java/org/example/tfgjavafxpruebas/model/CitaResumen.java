package org.example.tfgjavafxpruebas.model;


import javafx.beans.property.SimpleStringProperty;

public class CitaResumen {

    private final SimpleStringProperty cliente;
    private final SimpleStringProperty hora;
    private final SimpleStringProperty tipo;
    private final SimpleStringProperty estado;

    public CitaResumen(String cliente, String hora, String tipo, String estado) {
        this.cliente = new SimpleStringProperty(cliente);
        this.hora    = new SimpleStringProperty(hora);
        this.tipo    = new SimpleStringProperty(tipo);
        this.estado  = new SimpleStringProperty(estado);
    }

    public String getCliente() { return cliente.get(); }
    public String getHora()    { return hora.get(); }
    public String getTipo()    { return tipo.get(); }
    public String getEstado()  { return estado.get(); }

    public SimpleStringProperty clienteProperty() { return cliente; }
    public SimpleStringProperty horaProperty()    { return hora; }
    public SimpleStringProperty tipoProperty()    { return tipo; }
    public SimpleStringProperty estadoProperty()  { return estado; }
}
