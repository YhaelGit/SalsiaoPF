package org.example.salsiaopf.model;

import java.time.LocalDateTime;

public class PedidoVenta {
    private int idPedido;
    private String idFactura;
    private String clienteNombre;
    private String itemsTexto;
    private double total;
    private String estado;
    private LocalDateTime fechaCreacion;
    private String tipoVenta;
    private String metodoPago;
    private String observaciones;

    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public String getIdFactura() { return idFactura; }
    public void setIdFactura(String idFactura) { this.idFactura = idFactura; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getItemsTexto() { return itemsTexto; }
    public void setItemsTexto(String itemsTexto) { this.itemsTexto = itemsTexto; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getTipoVenta() { return tipoVenta; }
    public void setTipoVenta(String tipoVenta) { this.tipoVenta = tipoVenta; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstadoEmoji() {
        return switch (estado) {
            case "Pendiente" -> "\uD83D\uDFE1";
            case "Preparando" -> "\uD83D\uDD35";
            case "Listo" -> "\uD83D\uDFE2";
            case "En camino" -> "\uD83D\uDE9A";
            case "Entregado" -> "\u2705";
            case "Cancelado" -> "\u274C";
            default -> "\u26AA";
        };
    }
}
