package org.example.tfgjavafxpruebas.model;

public class AlertaStock {
    private String nombre;
    private int stockActual;
    private int stockMinimo;

    public AlertaStock(String nombre, int stockActual, int stockMinimo) {
        this.nombre      = nombre;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
    }

    public String getNombre()     { return nombre; }
    public int getStockActual()   { return stockActual; }
    public int getStockMinimo()   { return stockMinimo; }
}
