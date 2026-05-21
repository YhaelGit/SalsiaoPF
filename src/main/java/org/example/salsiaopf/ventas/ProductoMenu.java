package org.example.salsiaopf.ventas;

/**
 * Producto del menú digital de ventas (catálogo en memoria).
 */
public class ProductoMenu {

    private final String id;
    private final String nombre;
    private final String descripcion;
    private final String categoria;
    private final String emoji;
    private final double precio;
    private final String imagenPath;

    public ProductoMenu(String id, String nombre, String descripcion, String categoria, String emoji, double precio) {
        this(id, nombre, descripcion, categoria, emoji, precio, null);
    }

    public ProductoMenu(String id, String nombre, String descripcion, String categoria, String emoji, double precio, String imagenPath) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.emoji = emoji;
        this.precio = precio;
        this.imagenPath = imagenPath;
    }

    public String getImagenPath() {
        return imagenPath;
    }

    public boolean tieneImagen() {
        return imagenPath != null && !imagenPath.isBlank();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEmoji() {
        return emoji;
    }

    public double getPrecio() {
        return precio;
    }

    public String getPrecioFormateado() {
        return String.format("RD$ %,.0f", precio);
    }
}
