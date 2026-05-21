package org.example.salsiaopf.model;

import java.time.LocalDateTime;

public class PagoVenta {
    private int id;
    private int idVenta;
    private String metodo;
    private double monto;
    private double devuelta;
    private LocalDateTime fecha;
    private String referencia;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }
    public double getDevuelta() { return devuelta; }
    public void setDevuelta(double devuelta) { this.devuelta = devuelta; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
}
