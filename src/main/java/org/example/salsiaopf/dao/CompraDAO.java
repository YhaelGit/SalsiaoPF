package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    public static void crearTablasSiNoExisten() {
        String[] sqls = {
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_ORDEN_COMPRA')
            BEGIN
                CREATE TABLE tbl_ORDEN_COMPRA (
                    ID_orden INT IDENTITY(1,1) PRIMARY KEY,
                    Codigo_orden VARCHAR(20) NOT NULL UNIQUE,
                    fk_ID_proveedor INT NOT NULL,
                    Fecha DATE NOT NULL,
                    Notas VARCHAR(500) NULL,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
                    Total DECIMAL(10,2) NOT NULL DEFAULT 0,
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_DETALLE_ORDEN')
            BEGIN
                CREATE TABLE tbl_DETALLE_ORDEN (
                    ID_detalle INT IDENTITY(1,1) PRIMARY KEY,
                    fk_ID_orden INT NOT NULL,
                    fk_ID_ingrediente INT NOT NULL,
                    Cantidad DECIMAL(10,2) NOT NULL,
                    Unidad VARCHAR(10) NOT NULL,
                    Precio_unitario DECIMAL(10,2) NOT NULL,
                    Subtotal DECIMAL(10,2) NOT NULL
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_CONTADOR_ORDEN')
            BEGIN
                CREATE TABLE tbl_CONTADOR_ORDEN (
                    fecha DATE PRIMARY KEY,
                    ultimo_numero INT NOT NULL DEFAULT 0
                );
            END
            """
        };
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                for (String sql : sqls) stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error creando tablas: " + e.getMessage());
        }
    }

    public static synchronized String generarCodigoOrden() {
        String sqlGet = "SELECT ultimo_numero FROM tbl_CONTADOR_ORDEN WHERE fecha = ?";
        String sqlIns = "INSERT INTO tbl_CONTADOR_ORDEN (fecha, ultimo_numero) VALUES (?, 1)";
        String sqlUpd = "UPDATE tbl_CONTADOR_ORDEN SET ultimo_numero = ultimo_numero + 1 WHERE fecha = ?";

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "OC-00001";
            LocalDate hoy = LocalDate.now();
            int numero;
            try (PreparedStatement ps = conn.prepareStatement(sqlGet)) {
                ps.setDate(1, Date.valueOf(hoy));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    numero = rs.getInt(1) + 1;
                    try (PreparedStatement pu = conn.prepareStatement(sqlUpd)) {
                        pu.setDate(1, Date.valueOf(hoy));
                        pu.executeUpdate();
                    }
                } else {
                    numero = 1;
                    try (PreparedStatement pi = conn.prepareStatement(sqlIns)) {
                        pi.setDate(1, Date.valueOf(hoy));
                        pi.executeUpdate();
                    }
                }
            }
            return String.format("OC-%05d", numero);
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error generando código: " + e.getMessage());
            return "OC-" + System.currentTimeMillis() % 100000;
        }
    }

    public static int guardarOrden(int fkProveedor, LocalDate fecha, String notas, double total, List<DetalleOrden> detalles) {
        crearTablasSiNoExisten();
        String codigo = generarCodigoOrden();
        String sqlOrd = "INSERT INTO tbl_ORDEN_COMPRA (Codigo_orden, fk_ID_proveedor, Fecha, Notas, Estado, Total) OUTPUT INSERTED.ID_orden VALUES (?, ?, ?, ?, 'Pendiente', ?)";
        String sqlDet = "INSERT INTO tbl_DETALLE_ORDEN (fk_ID_orden, fk_ID_ingrediente, Cantidad, Unidad, Precio_unitario, Subtotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return -1;
            conn.setAutoCommit(false);
            try (PreparedStatement psOrd = conn.prepareStatement(sqlOrd)) {
                psOrd.setString(1, codigo);
                psOrd.setInt(2, fkProveedor);
                psOrd.setDate(3, Date.valueOf(fecha));
                psOrd.setString(4, notas.isEmpty() ? null : notas);
                psOrd.setDouble(5, total);
                ResultSet rs = psOrd.executeQuery();
                if (!rs.next()) { conn.rollback(); return -1; }
                int idOrden = rs.getInt(1);

                try (PreparedStatement psDet = conn.prepareStatement(sqlDet)) {
                    for (DetalleOrden d : detalles) {
                        psDet.setInt(1, idOrden);
                        psDet.setInt(2, d.idIngrediente);
                        psDet.setDouble(3, d.cantidad);
                        psDet.setString(4, d.unidad);
                        psDet.setDouble(5, d.precioUnitario);
                        psDet.setDouble(6, d.subtotal);
                        psDet.addBatch();
                    }
                    psDet.executeBatch();
                }
                conn.commit();
                return idOrden;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error guardando orden: " + e.getMessage());
            return -1;
        }
    }

    public static List<Object[]> listarOrdenes() {
        List<Object[]> filas = new ArrayList<>();
        String sql = """
            SELECT o.ID_orden, o.Codigo_orden, p.Nombre AS proveedor, o.Fecha, o.Estado, o.Total
            FROM tbl_ORDEN_COMPRA o
            LEFT JOIN tbl_PROVEEDOR p ON o.fk_ID_proveedor = p.ID_proveedor
            ORDER BY o.ID_orden DESC
            """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_orden"),
                    rs.getString("Codigo_orden"),
                    rs.getString("proveedor") != null ? rs.getString("proveedor") : "---",
                    rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : null,
                    rs.getString("Estado"),
                    rs.getDouble("Total")
                });
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error listando: " + e.getMessage());
        }
        return filas;
    }

    public static List<Object[]> listarDetallesOrden(int idOrden) {
        List<Object[]> filas = new ArrayList<>();
        String sql = """
            SELECT d.ID_detalle, i.Nom_Ingrediente, d.Cantidad, d.Unidad, d.Precio_unitario, d.Subtotal
            FROM tbl_DETALLE_ORDEN d
            LEFT JOIN tbl_INGREDIENTE i ON d.fk_ID_ingrediente = i.ID_ingredientes
            WHERE d.fk_ID_orden = ?
            """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return filas;
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[]{
                        rs.getInt("ID_detalle"),
                        rs.getString("Nom_Ingrediente") != null ? rs.getString("Nom_Ingrediente") : "---",
                        rs.getDouble("Cantidad"),
                        rs.getString("Unidad"),
                        rs.getDouble("Precio_unitario"),
                        rs.getDouble("Subtotal")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error listando detalles: " + e.getMessage());
        }
        return filas;
    }

    public static boolean marcarRecibida(int idOrden) {
        String sql = "UPDATE tbl_ORDEN_COMPRA SET Estado = 'Recibida' WHERE ID_orden = ? AND Estado = 'Pendiente'";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, idOrden);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error marcando recibida: " + e.getMessage());
            return false;
        }
    }

    public static boolean cancelarOrden(int idOrden) {
        String sqlDel = "DELETE FROM tbl_DETALLE_ORDEN WHERE fk_ID_orden = ?";
        String sqlOrd = "DELETE FROM tbl_ORDEN_COMPRA WHERE ID_orden = ?";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDel);
                 PreparedStatement ps2 = conn.prepareStatement(sqlOrd)) {
                ps1.setInt(1, idOrden);
                ps1.executeUpdate();
                ps2.setInt(1, idOrden);
                boolean ok = ps2.executeUpdate() > 0;
                conn.commit();
                return ok;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error cancelando: " + e.getMessage());
            return false;
        }
    }

    public static Object[] obtenerOrden(int idOrden) {
        String sql = """
            SELECT o.ID_orden, o.Codigo_orden, o.fk_ID_proveedor, p.Nombre AS proveedor, o.Fecha, o.Notas, o.Estado, o.Total
            FROM tbl_ORDEN_COMPRA o
            LEFT JOIN tbl_PROVEEDOR p ON o.fk_ID_proveedor = p.ID_proveedor
            WHERE o.ID_orden = ?
            """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null;
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("ID_orden"),
                        rs.getString("Codigo_orden"),
                        rs.getInt("fk_ID_proveedor"),
                        rs.getString("proveedor"),
                        rs.getDate("Fecha").toLocalDate(),
                        rs.getString("Notas") != null ? rs.getString("Notas") : "",
                        rs.getString("Estado"),
                        rs.getDouble("Total")
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error obteniendo orden: " + e.getMessage());
        }
        return null;
    }

    public static int guardarPago(int fkIdOrden, LocalDate fecha, double monto, String metodo) {
        String sql = "INSERT INTO tbl_PAGO_COMPRA (fk_ID_factura_compra, Fecha, Monto_pago, Estado, Metodo_pago) VALUES (?, ?, ?, 'Pagado', ?)";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, fkIdOrden);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setDouble(3, monto);
            ps.setString(4, metodo);
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[CompraDAO] Error guardando pago: " + e.getMessage());
            return -1;
        }
    }

    public static class DetalleOrden {
        public final int idIngrediente;
        public final double cantidad;
        public final String unidad;
        public final double precioUnitario;
        public final double subtotal;

        public DetalleOrden(int idIngrediente, double cantidad, String unidad, double precioUnitario) {
            this.idIngrediente = idIngrediente;
            this.cantidad = cantidad;
            this.unidad = unidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = cantidad * precioUnitario;
        }
    }
}
