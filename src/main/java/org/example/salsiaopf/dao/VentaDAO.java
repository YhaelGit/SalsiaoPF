package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.ventas.DatosProcesoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Acceso a datos de venta (tbl_VENTA_POS).
 */
public class VentaDAO {

    private static final String SQL_CREAR_VENTA = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_VENTA_POS')
            BEGIN
                CREATE TABLE tbl_VENTA_POS (
                    id_venta       INT IDENTITY(1,1) PRIMARY KEY,
                    id_factura     VARCHAR(40)    NOT NULL,
                    fecha_venta    DATETIME       NOT NULL DEFAULT GETDATE(),
                    total          DECIMAL(12,2)  NOT NULL,
                    metodo_pago    VARCHAR(30)    NOT NULL,
                    monto_recibido DECIMAL(12,2)  NULL,
                    devuelta       DECIMAL(12,2)  NULL,
                    email_cliente  VARCHAR(150)   NULL,
                    id_cliente     INT            NULL DEFAULT 1
                );
            END
            """;

    private static final String SQL_CREAR_DETALLE = """
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
                    CONSTRAINT FK_detalle_venta_pos
                        FOREIGN KEY (id_venta) REFERENCES tbl_VENTA_POS(id_venta)
                );
            END
            """;

    private static final String SQL_INSERT = """
            INSERT INTO tbl_VENTA_POS
            (id_factura, fecha_venta, total, metodo_pago, monto_recibido, devuelta, email_cliente, id_cliente)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private VentaDAO() {
    }

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(SQL_CREAR_VENTA);
                st.execute(SQL_CREAR_DETALLE);
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error creando tablas: " + e.getMessage());
        }
    }

    /**
     * Inserta la venta dentro de una transacción abierta.
     *
     * @return id_venta generado
     */
    public static int insertarVenta(Connection conn, String idFactura, DatosProcesoPago pago, int idCliente)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, idFactura);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(3, pago.getTotal());
            ps.setString(4, pago.getMetodoPago().getEtiqueta());

            if (pago.getMetodoPago() == org.example.salsiaopf.ventas.MetodoPago.EFECTIVO) {
                ps.setDouble(5, pago.getMontoRecibido());
                ps.setDouble(6, pago.getDevuelta());
            } else {
                ps.setNull(5, Types.DECIMAL);
                ps.setNull(6, Types.DECIMAL);
            }

            ps.setString(7, pago.getEmailCliente());
            ps.setInt(8, idCliente);

            if (ps.executeUpdate() == 0) {
                throw new SQLException("No se insertó la venta.");
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT SCOPE_IDENTITY()")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("No se obtuvo el ID de la venta.");
    }

    /** Compatibilidad con tbl_VENTA legacy. */
    public static boolean guardarVenta(int idCliente, double total) {
        String sql = "INSERT INTO tbl_VENTA (ID_cliente, total) VALUES (?, ?)";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                ps.setDouble(2, total);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error guardando venta legacy: " + e.getMessage());
            return false;
        }
    }
}
