package org.example.salsiaopf.model;

import java.time.LocalDateTime;

/**
 * @deprecated Sin uso actual en el sistema. Clase mantenida por compatibilidad futura.
 */
@Deprecated
public class Pedido {
    private int id;
    private int idCliente;
    private String clienteNombre;
    private LocalDateTime fecha;
    private String estado;
    private double total;
    private String tipoEntrega;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }
}
