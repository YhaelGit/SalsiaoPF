package org.example.salsiaopf.ventas;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarritoVentas {
    public static final double ITBIS_TASA = 0.18;

    private final ObservableList<ItemCarrito> items = FXCollections.observableArrayList();
    private final DoubleProperty descuento = new SimpleDoubleProperty(0);
    private final DoubleProperty delivery = new SimpleDoubleProperty(0);
    private final StringProperty tipoVenta = new SimpleStringProperty("Mostrador");

    public ObservableList<ItemCarrito> getItems() { return items; }
    public double getDescuento() { return descuento.get(); }
    public void setDescuento(double d) { descuento.set(Math.max(0, d)); }
    public DoubleProperty descuentoProperty() { return descuento; }
    public double getDelivery() { return delivery.get(); }
    public void setDelivery(double d) { delivery.set(Math.max(0, d)); }
    public DoubleProperty deliveryProperty() { return delivery; }
    public String getTipoVenta() { return tipoVenta.get(); }
    public void setTipoVenta(String t) { tipoVenta.set(t); }
    public StringProperty tipoVentaProperty() { return tipoVenta; }

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

    public void eliminar(ItemCarrito item) { items.remove(item); }
    public void vaciar() { items.clear(); descuento.set(0); delivery.set(0); }

    public int getCantidadTotalItems() {
        return items.stream().mapToInt(ItemCarrito::getCantidad).sum();
    }

    public double getSubtotal() {
        return items.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
    }

    public double getITBIS() {
        return (getSubtotal() - getDescuento()) * ITBIS_TASA;
    }

    public double getTotal() {
        return getSubtotal() - getDescuento() + getITBIS() + getDelivery();
    }

    public boolean estaVacio() { return items.isEmpty(); }

    public List<ItemCarrito> copiarItems() {
        List<ItemCarrito> copia = new ArrayList<>();
        for (ItemCarrito item : items) {
            ItemCarrito c = new ItemCarrito(item.getProducto(), item.getCantidad());
            c.getExtras().addAll(item.getExtras());
            c.setNota(item.getNota());
            copia.add(c);
        }
        return copia;
    }
}
