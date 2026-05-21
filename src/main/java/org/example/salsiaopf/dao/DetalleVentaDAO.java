package org.example.salsiaopf.dao;

import org.example.salsiaopf.ventas.ItemCarrito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos del detalle de venta (tbl_DETALLE_VENTA_POS).
 */
public class DetalleVentaDAO {

    private static final String SQL_INSERT = """
            INSERT INTO tbl_DETALLE_VENTA_POS
            (id_venta, producto_id, nombre_producto, cantidad, precio_unitario, subtotal)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private DetalleVentaDAO() {
    }

    public static void insertarDetalles(Connection conn, int idVenta, List<ItemCarrito> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new SQLException("No hay productos en el carrito para guardar.");
        }

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            for (ItemCarrito item : items) {
                ps.setInt(1, idVenta);
                ps.setString(2, item.getProducto().getId());
                ps.setString(3, item.getProducto().getNombre());
                ps.setInt(4, item.getCantidad());
                ps.setDouble(5, item.getProducto().getPrecio());
                ps.setDouble(6, item.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
