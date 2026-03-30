package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.SimpleStringProperty;

public class ReparacionResumen {
    private final SimpleStringProperty vehiculo;
    private final SimpleStringProperty mecanico;
    private final SimpleStringProperty inicio;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty coste;

    public ReparacionResumen(String vehiculo, String mecanico,
                             String inicio, String estado, String coste) {
        this.vehiculo  = new SimpleStringProperty(vehiculo);
        this.mecanico  = new SimpleStringProperty(mecanico);
        this.inicio    = new SimpleStringProperty(inicio);
        this.estado    = new SimpleStringProperty(estado);
        this.coste     = new SimpleStringProperty(coste);
    }

    public String getVehiculo()  { return vehiculo.get(); }
    public String getMecanico()  { return mecanico.get(); }
    public String getInicio()    { return inicio.get(); }
    public String getEstado()    { return estado.get(); }
    public String getCoste()     { return coste.get(); }

    public SimpleStringProperty vehiculoProperty()  { return vehiculo; }
    public SimpleStringProperty mecanicoProperty()  { return mecanico; }
    public SimpleStringProperty inicioProperty()    { return inicio; }
    public SimpleStringProperty estadoProperty()    { return estado; }
    public SimpleStringProperty costeProperty()     { return coste; }
}
