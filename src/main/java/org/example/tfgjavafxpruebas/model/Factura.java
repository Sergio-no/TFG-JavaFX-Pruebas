package org.example.tfgjavafxpruebas.model;

import javafx.beans.property.*;

public class Factura {
    private Long id;
    private final SimpleStringProperty numeroFactura = new SimpleStringProperty();
    private final SimpleStringProperty cliente       = new SimpleStringProperty();
    private final SimpleStringProperty fecha         = new SimpleStringProperty();
    private final SimpleStringProperty total         = new SimpleStringProperty();
    private final SimpleStringProperty metodoPago    = new SimpleStringProperty();
    private final SimpleStringProperty estado        = new SimpleStringProperty();

    public Factura() {}
    public Factura(Long id, String numero, String cliente, String fecha,
                   String total, String metodoPago, boolean pagada) {
        this.id = id;
        this.numeroFactura.set(numero);
        this.cliente.set(cliente);
        this.fecha.set(fecha);
        this.total.set(total);
        this.metodoPago.set(metodoPago == null ? "—" : metodoPago);
        this.estado.set(pagada ? "PAGADA" : "PENDIENTE");
    }

    public Long getId()             { return id; }
    public String getNumeroFactura(){ return numeroFactura.get(); }
    public String getCliente()      { return cliente.get(); }
    public String getFecha()        { return fecha.get(); }
    public String getTotal()        { return total.get(); }
    public String getMetodoPago()   { return metodoPago.get(); }
    public String getEstado()       { return estado.get(); }

    public SimpleStringProperty numeroFacturaProperty(){ return numeroFactura; }
    public SimpleStringProperty clienteProperty()      { return cliente; }
    public SimpleStringProperty fechaProperty()        { return fecha; }
    public SimpleStringProperty totalProperty()        { return total; }
    public SimpleStringProperty metodoPagoProperty()   { return metodoPago; }
    public SimpleStringProperty estadoProperty()       { return estado; }
}
