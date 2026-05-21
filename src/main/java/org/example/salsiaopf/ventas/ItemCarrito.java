package org.example.salsiaopf.ventas;

/**
 * Línea del carrito de ventas.
 */
public class ItemCarrito {

    private final ProductoMenu producto;
    private int cantidad;

    public ItemCarrito(ProductoMenu producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public ProductoMenu getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = Math.max(1, cantidad);
    }

    public void incrementar() {
        cantidad++;
    }

    public void decrementar() {
        if (cantidad > 1) {
            cantidad--;
        }
    }

    public double getSubtotal() {
        return producto.getPrecio() * cantidad;
    }
}
