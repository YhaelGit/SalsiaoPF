package org.example.salsiaopf.model;

import java.time.LocalDateTime;

/**
 * @deprecated Sin uso actual en el sistema. Clase mantenida por compatibilidad futura.
 */
@Deprecated
public class FacturaVenta {
    private int id;
    private int idVenta;
    private String numeroFactura;
    private LocalDateTime fecha;
    private double total;
    private String pdfRuta;
    private boolean correoEnviado;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int idVenta) { this.idVenta = idVenta; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getPdfRuta() { return pdfRuta; }
    public void setPdfRuta(String pdfRuta) { this.pdfRuta = pdfRuta; }
    public boolean isCorreoEnviado() { return correoEnviado; }
    public void setCorreoEnviado(boolean correoEnviado) { this.correoEnviado = correoEnviado; }
}
