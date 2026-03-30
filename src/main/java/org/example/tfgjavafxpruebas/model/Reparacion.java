package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.SimpleStringProperty;

public class Reparacion {
    private Long id;
    private final SimpleStringProperty vehiculo   = new SimpleStringProperty();
    private final SimpleStringProperty mecanico   = new SimpleStringProperty();
    private final SimpleStringProperty fechaInicio= new SimpleStringProperty();
    private final SimpleStringProperty fechaFin   = new SimpleStringProperty();
    private final SimpleStringProperty estado     = new SimpleStringProperty();
    private final SimpleStringProperty costeTotal = new SimpleStringProperty();

    public Reparacion() {}
    public Reparacion(Long id, String vehiculo, String mecanico,
                      String fechaInicio, String fechaFin,
                      String estado, String costeTotal) {
        this.id = id;
        this.vehiculo.set(vehiculo);
        this.mecanico.set(mecanico);
        this.fechaInicio.set(fechaInicio);
        this.fechaFin.set(fechaFin);
        this.estado.set(estado);
        this.costeTotal.set(costeTotal);
    }

    public Long getId() { return id; }
    public String getVehiculo()    { return vehiculo.get(); }
    public String getMecanico()    { return mecanico.get(); }
    public String getFechaInicio() { return fechaInicio.get(); }
    public String getFechaFin()    { return fechaFin.get(); }
    public String getEstado()      { return estado.get(); }
    public String getCosteTotal()  { return costeTotal.get(); }

    public SimpleStringProperty vehiculoProperty()    { return vehiculo; }
    public SimpleStringProperty mecanicoProperty()    { return mecanico; }
    public SimpleStringProperty fechaInicioProperty() { return fechaInicio; }
    public SimpleStringProperty fechaFinProperty()    { return fechaFin; }
    public SimpleStringProperty estadoProperty()      { return estado; }
    public SimpleStringProperty costeTotalProperty()  { return costeTotal; }
}
