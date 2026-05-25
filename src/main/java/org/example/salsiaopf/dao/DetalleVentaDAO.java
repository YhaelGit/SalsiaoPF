package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.ventas.ItemCarrito;

import java.sql.*;
import java.util.List;

public class DetalleVentaDAO {

    private static final String SQL_CREAR_TABLA = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_DETALLE_VENTA_POS')
            BEGIN
                CREATE TABLE tbl_DETALLE_VENTA_POS (
                    id_detalle       INT IDENTITY(1,1) PRIMARY KEY,
                    id_venta         INT NOT NULL,
                    producto_id      VARCHAR(50)   NOT NULL,
                    nombre_producto  VARCHAR(200)  NOT NULL,
                    cantidad         INT           NOT NULL,
                    precio_unitario  DECIMAL(12,2) NOT NULL,
                    subtotal         DECIMAL(12,2) NOT NULL,
                    extras           VARCHAR(500)  NULL,
                    nota             VARCHAR(300)  NULL,
                    CONSTRAINT FK_detalle_venta_pos
                        FOREIGN KEY (id_venta) REFERENCES tbl_VENTA_POS(id_venta)
                );
            END
            """;

    private static final String SQL_INSERT = """
            INSERT INTO tbl_DETALLE_VENTA_POS
            (id_venta, producto_id, nombre_producto, cantidad, precio_unitario, subtotal, extras, nota)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private DetalleVentaDAO() {}

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(SQL_CREAR_TABLA);
            }
        } catch (SQLException e) {
            System.out.println("[DetalleVentaDAO] Error: " + e.getMessage());
        }
    }

    public static void insertarDetalles(Connection conn, int idVenta, List<ItemCarrito> items) throws SQLException {
        if (items == null || items.isEmpty()) {
            throw new SQLException("No hay productos en el carrito.");
        }
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            for (ItemCarrito item : items) {
                ps.setInt(1, idVenta);
                ps.setString(2, item.getProducto().getId());
                ps.setString(3, item.getProducto().getNombre());
                ps.setInt(4, item.getCantidad());
                ps.setDouble(5, item.getPrecioUnitario());
                ps.setDouble(6, item.getSubtotal());
                String extrasStr = item.getExtras().isEmpty() ? null : String.join(", ", item.getExtras());
                ps.setString(7, extrasStr);
                ps.setString(8, item.getNota().isBlank() ? null : item.getNota());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
