package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.*;

public class Cliente {
    private Long id;
    private final SimpleStringProperty  nombre          = new SimpleStringProperty();
    private final SimpleStringProperty  email           = new SimpleStringProperty();
    private final SimpleStringProperty  telefono        = new SimpleStringProperty();
    private final SimpleIntegerProperty puntosAcumulados= new SimpleIntegerProperty();
    private final SimpleStringProperty  totalGastado    = new SimpleStringProperty();
    private final SimpleStringProperty  fechaRegistro   = new SimpleStringProperty();

    public Cliente() {}
    public Cliente(Long id, String nombre, String email, String telefono,
                   int puntos, String totalGastado, String fechaRegistro) {
        this.id = id;
        this.nombre.set(nombre);
        this.email.set(email);
        this.telefono.set(telefono);
        this.puntosAcumulados.set(puntos);
        this.totalGastado.set(totalGastado);
        this.fechaRegistro.set(fechaRegistro);
    }

    public Long getId()              { return id; }
    public String getNombre()        { return nombre.get(); }
    public String getEmail()         { return email.get(); }
    public String getTelefono()      { return telefono.get(); }
    public int getPuntosAcumulados() { return puntosAcumulados.get(); }
    public String getTotalGastado()  { return totalGastado.get(); }
    public String getFechaRegistro() { return fechaRegistro.get(); }

    public SimpleStringProperty  nombreProperty()         { return nombre; }
    public SimpleStringProperty  emailProperty()          { return email; }
    public SimpleStringProperty  telefonoProperty()       { return telefono; }
    public SimpleIntegerProperty puntosAcumuladosProperty(){ return puntosAcumulados; }
    public SimpleStringProperty  totalGastadoProperty()   { return totalGastado; }
    public SimpleStringProperty  fechaRegistroProperty()  { return fechaRegistro; }
}
