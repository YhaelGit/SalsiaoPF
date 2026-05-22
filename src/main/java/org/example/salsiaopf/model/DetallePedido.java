package org.example.salsiaopf.model;

/**
 * @deprecated Sin uso actual en el sistema. Clase mantenida por compatibilidad futura.
 */
@Deprecated
public class DetallePedido {
    private int id;
    private int idPedido;
    private int idProducto;
    private String nombreProducto;
    private int cantidad;
    private double precio;
    private double subtotal;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdPedido() { return idPedido; }
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
