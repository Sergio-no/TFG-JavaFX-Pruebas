package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.*;

public class MecanicoStats {
    private final SimpleStringProperty  nombre     = new SimpleStringProperty();
    private final SimpleIntegerProperty reparaciones = new SimpleIntegerProperty();
    private final SimpleStringProperty  ingresos   = new SimpleStringProperty();

    public MecanicoStats(String nombre, int reparaciones, String ingresos) {
        this.nombre.set(nombre);
        this.reparaciones.set(reparaciones);
        this.ingresos.set(ingresos);
    }

    public String getNombre()       { return nombre.get(); }
    public int getReparaciones()    { return reparaciones.get(); }
    public String getIngresos()     { return ingresos.get(); }

    public SimpleStringProperty  nombreProperty()      { return nombre; }
    public SimpleIntegerProperty reparacionesProperty(){ return reparaciones; }
    public SimpleStringProperty  ingresosProperty()    { return ingresos; }
}
