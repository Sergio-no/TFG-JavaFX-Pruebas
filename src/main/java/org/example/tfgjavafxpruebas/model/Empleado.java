package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.*;

public class Empleado {
    private Long id;
    private final SimpleStringProperty nombre   = new SimpleStringProperty();
    private final SimpleStringProperty email    = new SimpleStringProperty();
    private final SimpleStringProperty telefono = new SimpleStringProperty();
    private final SimpleStringProperty rol      = new SimpleStringProperty();
    private final SimpleBooleanProperty activo  = new SimpleBooleanProperty();

    public Empleado() {}
    public Empleado(Long id, String nombre, String email,
                    String telefono, String rol, boolean activo) {
        this.id = id;
        this.nombre.set(nombre);
        this.email.set(email);
        this.telefono.set(telefono);
        this.rol.set(rol);
        this.activo.set(activo);
    }

    public Long getId()        { return id; }
    public String getNombre()  { return nombre.get(); }
    public String getEmail()   { return email.get(); }
    public String getTelefono(){ return telefono.get(); }
    public String getRol()     { return rol.get(); }
    public boolean isActivo()  { return activo.get(); }
    public String getEstado()  { return activo.get() ? "Activo" : "Inactivo"; }

    public SimpleStringProperty  nombreProperty()   { return nombre; }
    public SimpleStringProperty  emailProperty()    { return email; }
    public SimpleStringProperty  telefonoProperty() { return telefono; }
    public SimpleStringProperty  rolProperty()      { return rol; }
    public SimpleBooleanProperty activoProperty()   { return activo; }
}

