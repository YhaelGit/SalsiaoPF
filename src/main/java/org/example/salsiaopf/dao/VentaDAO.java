package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentaDAO {

    private static final String SQL_CREAR_TABLA = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_VENTA_POS')
            BEGIN
                CREATE TABLE tbl_VENTA_POS (
                    id_venta       INT IDENTITY(1,1) PRIMARY KEY,
                    id_factura     VARCHAR(40)    NOT NULL,
                    fecha_venta    DATETIME       NOT NULL DEFAULT GETDATE(),
                    tipo_venta     VARCHAR(30)    NOT NULL DEFAULT 'Mostrador',
                    subtotal       DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    itbis          DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    descuento      DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    delivery       DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    total          DECIMAL(12,2)  NOT NULL,
                    metodo_pago    VARCHAR(30)    NOT NULL,
                    monto_recibido DECIMAL(12,2)  NULL,
                    devuelta       DECIMAL(12,2)  NULL,
                    email_cliente  VARCHAR(150)   NULL,
                    id_cliente     INT            NULL DEFAULT 1,
                    estado         VARCHAR(20)    NOT NULL DEFAULT 'Completada',
                    observaciones  VARCHAR(500)   NULL
                );
            END
            """;

    private static final String SQL_INSERT = """
            INSERT INTO tbl_VENTA_POS
            (id_factura, fecha_venta, tipo_venta, subtotal, itbis, descuento, delivery, total,
             metodo_pago, monto_recibido, devuelta, email_cliente, id_cliente, estado, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final DateTimeFormatter ID_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private VentaDAO() {}

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(SQL_CREAR_TABLA);
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error creando tabla: " + e.getMessage());
        }
    }

    public static String generarIdFactura() {
        return "FACT-" + LocalDateTime.now().format(ID_FMT);
    }

    public static int insertarVenta(Connection conn, String idFactura, String tipoVenta,
                                     double subtotal, double itbis, double descuento, double delivery,
                                     double total, String metodoPago, double montoRecibido,
                                     double devuelta, String email, int idCliente) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, idFactura);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, tipoVenta);
            ps.setDouble(4, subtotal);
            ps.setDouble(5, itbis);
            ps.setDouble(6, descuento);
            ps.setDouble(7, delivery);
            ps.setDouble(8, total);
            ps.setString(9, metodoPago);
            if (montoRecibido > 0) {
                ps.setDouble(10, montoRecibido);
                ps.setDouble(11, devuelta);
            } else {
                ps.setNull(10, Types.DECIMAL);
                ps.setNull(11, Types.DECIMAL);
            }
            ps.setString(12, email);
            ps.setInt(13, idCliente);
            ps.setString(14, "Completada");
            ps.setString(15, "");
            if (ps.executeUpdate() == 0) throw new SQLException("No se insertó la venta.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT SCOPE_IDENTITY()")) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("No se obtuvo el ID de la venta.");
    }

    public static List<Map<String, Object>> obtenerVentasRecientes(int limite) {
        List<Map<String, Object>> ventas = new ArrayList<>();
        String sql = "SELECT TOP (?) v.*, ISNULL(c.Nombre + ' ' + c.Apellido, 'Mostrador') AS cliente_nombre " +
                     "FROM tbl_VENTA_POS v " +
                     "LEFT JOIN tbl_CLIENTE c ON v.id_cliente = c.ID_cliente " +
                     "ORDER BY v.id_venta DESC";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return ventas;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, limite);
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            row.put(md.getColumnName(i), rs.getObject(i));
                        }
                        ventas.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error obteniendo ventas: " + e.getMessage());
        }
        return ventas;
    }

    public static List<Map<String, Object>> obtenerVentasPorRango(LocalDate desde, LocalDate hasta) {
        List<Map<String, Object>> ventas = new ArrayList<>();
        String sql = "SELECT v.*, ISNULL(c.Nombre + ' ' + c.Apellido, 'Mostrador') AS cliente_nombre " +
                     "FROM tbl_VENTA_POS v " +
                     "LEFT JOIN tbl_CLIENTE c ON v.id_cliente = c.ID_cliente " +
                     "WHERE CAST(v.fecha_venta AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY v.id_venta DESC";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return ventas;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(desde));
                ps.setDate(2, Date.valueOf(hasta));
                try (ResultSet rs = ps.executeQuery()) {
                    ResultSetMetaData md = rs.getMetaData();
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            row.put(md.getColumnName(i), rs.getObject(i));
                        }
                        ventas.add(row);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error filtrando ventas: " + e.getMessage());
        }
        return ventas;
    }

    public static double obtenerTotalVentasHoy() {
        String sql = "SELECT ISNULL(SUM(total), 0) FROM tbl_VENTA_POS WHERE CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return 0;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error: " + e.getMessage());
        }
        return 0;
    }

    public static int obtenerCantidadVentasHoy() {
        String sql = "SELECT COUNT(*) FROM tbl_VENTA_POS WHERE CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return 0;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error: " + e.getMessage());
        }
        return 0;
    }

    public static java.util.LinkedHashMap<LocalDate, Double> obtenerVentasPorSemana() {
        java.util.LinkedHashMap<LocalDate, Double> result = new java.util.LinkedHashMap<>();
        LocalDate hoy = LocalDate.now();
        for (int i = 6; i >= 0; i--) result.put(hoy.minusDays(i), 0.0);
        String sql = "SELECT CAST(fecha_venta AS DATE) dia, ISNULL(SUM(total), 0) total FROM tbl_VENTA_POS " +
                     "WHERE fecha_venta >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) " +
                     "GROUP BY CAST(fecha_venta AS DATE) ORDER BY dia";
        try (Connection conn = ConexionBD.conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (conn == null) return result;
            while (rs.next()) result.put(rs.getDate("dia").toLocalDate(), rs.getDouble("total"));
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error ventas semanales: " + e.getMessage());
        }
        return result;
    }

    public static List<String[]> obtenerTopProductos(int limite) {
        List<String[]> top = new ArrayList<>();
        String sql = "SELECT TOP (?) d.nombre_producto, SUM(d.cantidad) AS total_vendido " +
                     "FROM tbl_DETALLE_VENTA_POS d " +
                     "INNER JOIN tbl_VENTA_POS v ON d.id_venta = v.id_venta " +
                     "WHERE v.fecha_venta >= DATEADD(DAY, -30, CAST(GETDATE() AS DATE)) " +
                     "GROUP BY d.nombre_producto ORDER BY total_vendido DESC";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return top;
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) top.add(new String[]{rs.getString("nombre_producto"), String.valueOf(rs.getInt("total_vendido"))});
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error top productos: " + e.getMessage());
        }
        return top;
    }

    public static int obtenerCantidadClientes() {
        String sql = "SELECT COUNT(*) FROM tbl_CLIENTE";
        try (Connection conn = ConexionBD.conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (conn == null) return 0;
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error contando clientes: " + e.getMessage());
        }
        return 0;
    }

    public static Map<String, Double> obtenerTotalesHoyPorMetodo() {
        Map<String, Double> result = new HashMap<>();
        result.put("Efectivo", 0.0);
        result.put("Tarjeta", 0.0);
        result.put("Transferencia", 0.0);
        String sql = "SELECT metodo_pago, ISNULL(SUM(total), 0) AS total FROM tbl_VENTA_POS " +
                     "WHERE CAST(fecha_venta AS DATE) = CAST(GETDATE() AS DATE) " +
                     "GROUP BY metodo_pago";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return result;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) result.put(rs.getString("metodo_pago"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("[VentaDAO] Error obteniendo totales por metodo: " + e.getMessage());
        }
        return result;
    }

    public static int insertarVenta(Connection conn, String idFactura, org.example.salsiaopf.ventas.DatosProcesoPago pago, int idCliente) throws SQLException {
        return insertarVenta(conn, idFactura, "Mostrador",
            pago.getTotal(), 0, 0, 0,
            pago.getTotal(), pago.getMetodoPago().getEtiqueta(),
            pago.getMontoRecibido(), pago.getDevuelta(),
            pago.getEmailCliente(), idCliente);
    }

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
            System.out.println("[VentaDAO] Error guardando venta legacy: " + e.getMessage());
            return false;
        }
    }
}
