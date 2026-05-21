package org.example.salsiaopf.ventas;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Carrito de compras en memoria para el punto de venta.
 */
public class CarritoVentas {

    private final ObservableList<ItemCarrito> items = FXCollections.observableArrayList();

    public ObservableList<ItemCarrito> getItems() {
        return items;
    }

    public void agregar(ProductoMenu producto) {
        Optional<ItemCarrito> existente = items.stream()
                .filter(i -> i.getProducto().getId().equals(producto.getId()))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().incrementar();
        } else {
            items.add(new ItemCarrito(producto, 1));
        }
    }

    public void eliminar(ItemCarrito item) {
        items.remove(item);
    }

    public void vaciar() {
        items.clear();
    }

    public int getCantidadTotalItems() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    /** Copia los ítems actuales (para guardar venta sin perder el carrito antes de confirmar). */
    public List<ItemCarrito> copiarItems() {
        List<ItemCarrito> copia = new ArrayList<>();
        for (ItemCarrito item : items) {
            copia.add(new ItemCarrito(item.getProducto(), item.getCantidad()));
        }
        return copia;
    }
}
