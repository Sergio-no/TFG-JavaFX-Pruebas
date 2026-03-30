package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.*;

public class Pieza {
    private Long id;
    private final SimpleStringProperty  nombre        = new SimpleStringProperty();
    private final SimpleStringProperty  descripcion   = new SimpleStringProperty();
    private final SimpleDoubleProperty  precioUnitario= new SimpleDoubleProperty();
    private final SimpleIntegerProperty stockActual   = new SimpleIntegerProperty();
    private final SimpleIntegerProperty stockMinimo   = new SimpleIntegerProperty();

    public Pieza() {}
    public Pieza(Long id, String nombre, String descripcion,
                 double precio, int stockActual, int stockMinimo) {
        this.id = id;
        this.nombre.set(nombre);
        this.descripcion.set(descripcion);
        this.precioUnitario.set(precio);
        this.stockActual.set(stockActual);
        this.stockMinimo.set(stockMinimo);
    }

    public Long getId()            { return id; }
    public String getNombre()      { return nombre.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public double getPrecioUnitario() { return precioUnitario.get(); }
    public int getStockActual()    { return stockActual.get(); }
    public int getStockMinimo()    { return stockMinimo.get(); }
    public String getEstadoStock() {
        if (stockActual.get() == 0)              return "Sin stock";
        if (stockActual.get() < stockMinimo.get()) return "Stock bajo";
        return "OK";
    }

    public SimpleStringProperty  nombreProperty()       { return nombre; }
    public SimpleStringProperty  descripcionProperty()  { return descripcion; }
    public SimpleDoubleProperty  precioUnitarioProperty(){ return precioUnitario; }
    public SimpleIntegerProperty stockActualProperty()  { return stockActual; }
    public SimpleIntegerProperty stockMinimoProperty()  { return stockMinimo; }
}
