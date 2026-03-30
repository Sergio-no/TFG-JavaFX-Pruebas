package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.SimpleStringProperty;

public class Cita {
    private Long id;
    private final SimpleStringProperty clienteNombre = new SimpleStringProperty();
    private final SimpleStringProperty vehiculo      = new SimpleStringProperty();
    private final SimpleStringProperty fecha         = new SimpleStringProperty();
    private final SimpleStringProperty hora          = new SimpleStringProperty();
    private final SimpleStringProperty tipo          = new SimpleStringProperty();
    private final SimpleStringProperty estado        = new SimpleStringProperty();
    private final SimpleStringProperty descripcion   = new SimpleStringProperty();

    public Cita() {}
    public Cita(Long id, String clienteNombre, String vehiculo,
                String fecha, String hora, String tipo,
                String estado, String descripcion) {
        this.id = id;
        this.clienteNombre.set(clienteNombre);
        this.vehiculo.set(vehiculo);
        this.fecha.set(fecha);
        this.hora.set(hora);
        this.tipo.set(tipo);
        this.estado.set(estado);
        this.descripcion.set(descripcion);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClienteNombre() { return clienteNombre.get(); }
    public String getVehiculo()      { return vehiculo.get(); }
    public String getFecha()         { return fecha.get(); }
    public String getHora()          { return hora.get(); }
    public String getTipo()          { return tipo.get(); }
    public String getEstado()        { return estado.get(); }
    public String getDescripcion()   { return descripcion.get(); }

    public SimpleStringProperty clienteNombreProperty() { return clienteNombre; }
    public SimpleStringProperty vehiculoProperty()      { return vehiculo; }
    public SimpleStringProperty fechaProperty()         { return fecha; }
    public SimpleStringProperty horaProperty()          { return hora; }
    public SimpleStringProperty tipoProperty()          { return tipo; }
    public SimpleStringProperty estadoProperty()        { return estado; }
    public SimpleStringProperty descripcionProperty()   { return descripcion; }
}
