package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.PedidoVenta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoVentaDAO {

    private static final String SQL_CREAR = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_PEDIDO_VENTA')
            BEGIN
                CREATE TABLE tbl_PEDIDO_VENTA (
                    id_pedido      INT IDENTITY(1,1) PRIMARY KEY,
                    id_factura     VARCHAR(40)    NOT NULL,
                    cliente_nombre VARCHAR(150)   NULL DEFAULT 'Consumidor Final',
                    items_texto    VARCHAR(1000)  NULL,
                    total          DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    estado         VARCHAR(20)    NOT NULL DEFAULT 'Pendiente',
                    fecha_creacion DATETIME       NOT NULL DEFAULT GETDATE(),
                    tipo_venta     VARCHAR(30)    NULL DEFAULT 'Mostrador',
                    metodo_pago    VARCHAR(30)    NULL,
                    observaciones  VARCHAR(500)   NULL
                );
            END
            """;

    private PedidoVentaDAO() {}

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(SQL_CREAR);
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error creando tabla: " + e.getMessage());
        }
    }

    public static int insertar(PedidoVenta p) {
        String sql = "INSERT INTO tbl_PEDIDO_VENTA (id_factura, cliente_nombre, items_texto, total, estado, tipo_venta, metodo_pago, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return -1;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getIdFactura());
                ps.setString(2, p.getClienteNombre());
                ps.setString(3, p.getItemsTexto());
                ps.setDouble(4, p.getTotal());
                ps.setString(5, p.getEstado());
                ps.setString(6, p.getTipoVenta());
                ps.setString(7, p.getMetodoPago());
                ps.setString(8, p.getObservaciones());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT SCOPE_IDENTITY()")) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error insertando: " + e.getMessage());
        }
        return -1;
    }

    public static boolean actualizarEstado(int idPedido, String nuevoEstado) {
        String sql = "UPDATE tbl_PEDIDO_VENTA SET estado = ? WHERE id_pedido = ?";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuevoEstado);
                ps.setInt(2, idPedido);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error actualizando estado: " + e.getMessage());
        }
        return false;
    }

    public static boolean eliminar(int idPedido) {
        String sql = "DELETE FROM tbl_PEDIDO_VENTA WHERE id_pedido = ?";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPedido);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error eliminando: " + e.getMessage());
        }
        return false;
    }

    public static List<PedidoVenta> listarTodos() {
        return listar(null, null);
    }

    public static List<PedidoVenta> listar(String filtroEstado, String busqueda) {
        return listar(null, filtroEstado, busqueda);
    }

    public static List<PedidoVenta> listar(String clienteNombre, String filtroEstado, String busqueda) {
        List<PedidoVenta> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tbl_PEDIDO_VENTA WHERE 1=1");
        if (clienteNombre != null && !clienteNombre.isBlank()) {
            sql.append(" AND cliente_nombre = ?");
        }
        if (filtroEstado != null && !filtroEstado.isEmpty() && !filtroEstado.equals("Todos")) {
            sql.append(" AND estado = ?");
        }
        if (busqueda != null && !busqueda.isBlank()) {
            sql.append(" AND (cliente_nombre LIKE ? OR id_factura LIKE ? OR items_texto LIKE ?)");
        }
        sql.append(" ORDER BY CASE estado WHEN 'Pendiente' THEN 0 WHEN 'Preparando' THEN 1 WHEN 'Listo' THEN 2 WHEN 'En camino' THEN 3 ELSE 4 END, fecha_creacion DESC");

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return lista;
            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int idx = 1;
                if (clienteNombre != null && !clienteNombre.isBlank()) {
                    ps.setString(idx++, clienteNombre.trim());
                }
                if (filtroEstado != null && !filtroEstado.isEmpty() && !filtroEstado.equals("Todos")) {
                    ps.setString(idx++, filtroEstado);
                }
                if (busqueda != null && !busqueda.isBlank()) {
                    String like = "%" + busqueda.trim() + "%";
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                    ps.setString(idx++, like);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error listando: " + e.getMessage());
        }
        return lista;
    }

    public static PedidoVenta obtenerPorId(int idPedido) {
        String sql = "SELECT * FROM tbl_PEDIDO_VENTA WHERE id_pedido = ?";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPedido);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapear(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("[PedidoVentaDAO] Error obteniendo: " + e.getMessage());
        }
        return null;
    }

    private static PedidoVenta mapear(ResultSet rs) throws SQLException {
        PedidoVenta p = new PedidoVenta();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setIdFactura(rs.getString("id_factura"));
        p.setClienteNombre(rs.getString("cliente_nombre"));
        p.setItemsTexto(rs.getString("items_texto"));
        p.setTotal(rs.getDouble("total"));
        p.setEstado(rs.getString("estado"));
        p.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        p.setTipoVenta(rs.getString("tipo_venta"));
        p.setMetodoPago(rs.getString("metodo_pago"));
        p.setObservaciones(rs.getString("observaciones"));
        return p;
    }
}
