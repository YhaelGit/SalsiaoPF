package org.example.salsiaopf.ventas;

import java.util.ArrayList;
import java.util.List;

public class ItemCarrito {
    private final ProductoMenu producto;
    private int cantidad;
    private final List<String> extras = new ArrayList<>();
    private String nota = "";

    public ItemCarrito(ProductoMenu producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public ProductoMenu getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = Math.max(1, cantidad); }
    public void incrementar() { cantidad++; }
    public void decrementar() { if (cantidad > 1) cantidad--; }
    public List<String> getExtras() { return extras; }
    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public double getSubtotal() {
        double extraTotal = extras.size() * 50.0;
        return (producto.getPrecio() + extraTotal) * cantidad;
    }

    public double getPrecioUnitario() {
        return producto.getPrecio() + extras.size() * 50.0;
    }
}
