package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Cliente;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClienteDAO {

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                // Tablas nuevas
                String[] sqls = {
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CLIENTE') BEGIN CREATE TABLE tbl_CLIENTE (ID_cliente INT IDENTITY(1,1) PRIMARY KEY, Codigo VARCHAR(20) NOT NULL UNIQUE, Nombre VARCHAR(100) NOT NULL, Apellido VARCHAR(100) NOT NULL, Cedula VARCHAR(20) NULL, Telefono VARCHAR(20) NOT NULL, Direccion VARCHAR(200) NULL, Email VARCHAR(100) NULL, Tipo_cliente VARCHAR(30) DEFAULT 'Regular', Estado VARCHAR(20) DEFAULT 'Activo', Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE(), ultima_compra DATE NULL, total_compras DECIMAL(12,2) DEFAULT 0, cantidad_compras INT DEFAULT 0) END",
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CONTADOR_CLIENTE') BEGIN CREATE TABLE tbl_CONTADOR_CLIENTE (ultimo_numero INT NOT NULL DEFAULT 0); INSERT INTO tbl_CONTADOR_CLIENTE VALUES (0) END",
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_DIRECCION_CLIENTE') BEGIN CREATE TABLE tbl_DIRECCION_CLIENTE (ID_direccion INT IDENTITY(1,1) PRIMARY KEY, fk_ID_cliente INT NOT NULL, Direccion VARCHAR(200) NOT NULL, Referencia VARCHAR(200) NULL, Zona VARCHAR(100) NULL, Telefono VARCHAR(20) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_RESERVA_CLIENTE') BEGIN CREATE TABLE tbl_RESERVA_CLIENTE (ID_reserva INT IDENTITY(1,1) PRIMARY KEY, Codigo VARCHAR(20) NOT NULL UNIQUE, fk_ID_cliente INT NOT NULL, Fecha DATE NOT NULL, Hora VARCHAR(10) NOT NULL, Cantidad_personas INT NOT NULL, Estado VARCHAR(20) DEFAULT 'Pendiente', Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
                    "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_COMPRA_CLIENTE') BEGIN CREATE TABLE tbl_COMPRA_CLIENTE (ID_compra INT IDENTITY(1,1) PRIMARY KEY, fk_ID_cliente INT NOT NULL, Fecha DATE NOT NULL, Numero_pedido VARCHAR(30) NOT NULL, Total DECIMAL(12,2) DEFAULT 0, Metodo_pago VARCHAR(30) NULL, Estado_pedido VARCHAR(20) DEFAULT 'Completado', fecha_creacion DATETIME DEFAULT GETDATE()) END"
                };
                for (String s : sqls) stmt.execute(s);
                // ALTER existing tbl_CLIENTE if columns missing
                String[] alters = {
                    "IF COL_LENGTH('tbl_CLIENTE','Codigo') IS NULL ALTER TABLE tbl_CLIENTE ADD Codigo VARCHAR(20) NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','Cedula') IS NULL ALTER TABLE tbl_CLIENTE ADD Cedula VARCHAR(20) NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','Direccion') IS NULL ALTER TABLE tbl_CLIENTE ADD Direccion VARCHAR(200) NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','Email') IS NULL ALTER TABLE tbl_CLIENTE ADD Email VARCHAR(100) NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','Tipo_cliente') IS NULL ALTER TABLE tbl_CLIENTE ADD Tipo_cliente VARCHAR(30) DEFAULT 'Regular'",
                    "IF COL_LENGTH('tbl_CLIENTE','Estado') IS NULL ALTER TABLE tbl_CLIENTE ADD Estado VARCHAR(20) DEFAULT 'Activo'",
                    "IF COL_LENGTH('tbl_CLIENTE','Observaciones') IS NULL ALTER TABLE tbl_CLIENTE ADD Observaciones VARCHAR(500) NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','ultima_compra') IS NULL ALTER TABLE tbl_CLIENTE ADD ultima_compra DATE NULL",
                    "IF COL_LENGTH('tbl_CLIENTE','total_compras') IS NULL ALTER TABLE tbl_CLIENTE ADD total_compras DECIMAL(12,2) DEFAULT 0",
                    "IF COL_LENGTH('tbl_CLIENTE','cantidad_compras') IS NULL ALTER TABLE tbl_CLIENTE ADD cantidad_compras INT DEFAULT 0",
                    "IF COL_LENGTH('tbl_CLIENTE','fecha_creacion') IS NULL ALTER TABLE tbl_CLIENTE ADD fecha_creacion DATETIME DEFAULT GETDATE()"
                };
                for (String a : alters) stmt.execute(a);
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error creando/alterando tablas: " + e.getMessage());
        }
    }

    public static synchronized String generarCodigoCliente() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "CLI-0001";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE tbl_CONTADOR_CLIENTE SET ultimo_numero = ultimo_numero + 1");
                ResultSet rs = stmt.executeQuery("SELECT ultimo_numero FROM tbl_CONTADOR_CLIENTE");
                return rs.next() ? String.format("CLI-%04d", rs.getInt(1)) : "CLI-0001";
            }
        } catch (SQLException e) {
            return "CLI-" + System.currentTimeMillis() % 10000;
        }
    }

    private static synchronized String generarCodigoReserva() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "RES-001";
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT ISNULL(MAX(ID_reserva),0)+1 FROM tbl_RESERVA_CLIENTE");
                return rs.next() ? String.format("RES-%04d", rs.getInt(1)) : "RES-0001";
            }
        } catch (SQLException e) {
            return "RES-" + System.currentTimeMillis() % 10000;
        }
    }

    // ── DASHBOARD ────────────────────────────────────────────────────

    public static Map<String, Object> obtenerConteosDashboard() {
        Map<String, Object> r = new HashMap<>();
        r.put("totalClientes", 0);
        r.put("frecuentes", 0);
        r.put("nuevosMes", 0);
        r.put("totalPedidos", 0);
        r.put("totalReservas", 0);
        r.put("topCompra", 0.0);
        String sql = "SELECT (SELECT COUNT(*) FROM tbl_CLIENTE) AS total, (SELECT COUNT(*) FROM tbl_CLIENTE WHERE cantidad_compras>=5) AS freq, (SELECT COUNT(*) FROM tbl_CLIENTE WHERE MONTH(fecha_creacion)=MONTH(GETDATE()) AND YEAR(fecha_creacion)=YEAR(GETDATE())) AS nuevos, (SELECT COUNT(*) FROM tbl_COMPRA_CLIENTE) AS pedidos, (SELECT COUNT(*) FROM tbl_RESERVA_CLIENTE) AS reservas, ISNULL((SELECT MAX(total_compras) FROM tbl_CLIENTE),0) AS topCompra";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return r;
            if (rs.next()) {
                r.put("totalClientes", rs.getInt("total"));
                r.put("frecuentes", rs.getInt("freq"));
                r.put("nuevosMes", rs.getInt("nuevos"));
                r.put("totalPedidos", rs.getInt("pedidos"));
                r.put("totalReservas", rs.getInt("reservas"));
                r.put("topCompra", rs.getDouble("topCompra"));
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error dashboard: " + e.getMessage());
        }
        return r;
    }

    // ── CLIENTES CRUD ───────────────────────────────────────────────

    public static int guardarCliente(String codigo, String nombre, String apellido, String cedula, String telefono, String direccion, String email, String tipo, String estado, String obs) {
        crearTablasSiNoExisten();
        String sql = "INSERT INTO tbl_CLIENTE (Codigo, Nombre, Apellido, Cedula, Telefono, Direccion, Email, Tipo_cliente, Estado, Observaciones) OUTPUT INSERTED.ID_cliente VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo); ps.setString(2, nombre); ps.setString(3, apellido);
            ps.setString(4, cedula.isEmpty()?null:cedula); ps.setString(5, telefono);
            ps.setString(6, direccion.isEmpty()?null:direccion); ps.setString(7, email.isEmpty()?null:email);
            ps.setString(8, tipo); ps.setString(9, estado); ps.setString(10, obs.isEmpty()?null:obs);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error guardando: " + e.getMessage());
            return -1;
        }
    }

    public static boolean actualizarCliente(int id, String nombre, String apellido, String cedula, String telefono, String direccion, String email, String tipo, String estado, String obs) {
        String sql = "UPDATE tbl_CLIENTE SET Nombre=?, Apellido=?, Cedula=?, Telefono=?, Direccion=?, Email=?, Tipo_cliente=?, Estado=?, Observaciones=? WHERE ID_cliente=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre); ps.setString(2, apellido);
            ps.setString(3, cedula.isEmpty()?null:cedula); ps.setString(4, telefono);
            ps.setString(5, direccion.isEmpty()?null:direccion); ps.setString(6, email.isEmpty()?null:email);
            ps.setString(7, tipo); ps.setString(8, estado); ps.setString(9, obs.isEmpty()?null:obs);
            ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error actualizando: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarCliente(int id) {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tbl_DIRECCION_CLIENTE WHERE fk_ID_cliente=?"); PreparedStatement ps2 = conn.prepareStatement("DELETE FROM tbl_RESERVA_CLIENTE WHERE fk_ID_cliente=?"); PreparedStatement ps3 = conn.prepareStatement("DELETE FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=?"); PreparedStatement ps4 = conn.prepareStatement("DELETE FROM tbl_CLIENTE WHERE ID_cliente=?")) {
                ps1.setInt(1, id); ps1.executeUpdate();
                ps2.setInt(1, id); ps2.executeUpdate();
                ps3.setInt(1, id); ps3.executeUpdate();
                ps4.setInt(1, id); ps4.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error eliminando: " + e.getMessage());
            return false;
        }
    }

    public static List<Cliente> listarClientes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT ID_cliente, Codigo, Nombre, Apellido, ISNULL(Cedula,'') AS Cedula, ISNULL(Telefono,'') AS Telefono, ISNULL(Direccion,'') AS Direccion, ISNULL(Email,'') AS Email, ISNULL(Tipo_cliente,'Regular') AS Tipo_cliente, ISNULL(Estado,'Activo') AS Estado, ISNULL(Observaciones,'') AS Observaciones, fecha_creacion, ultima_compra, ISNULL(total_compras,0) AS total_compras, ISNULL(cantidad_compras,0) AS cantidad_compras FROM tbl_CLIENTE ORDER BY ID_cliente ASC";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return lista;
            while (rs.next()) lista.add(mapearCliente(rs));
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error listando: " + e.getMessage());
        }
        return lista;
    }

    public static Cliente obtenerClientePorId(int id) {
        String sql = "SELECT * FROM tbl_CLIENTE WHERE ID_cliente=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null;
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearCliente(rs) : null;
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error obteniendo: " + e.getMessage());
            return null;
        }
    }

    public static List<Cliente> buscarClientes(String filtro) {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM tbl_CLIENTE WHERE Nombre LIKE ? OR Apellido LIKE ? OR Cedula LIKE ? OR Telefono LIKE ? OR Codigo LIKE ? ORDER BY ID_cliente DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return lista;
            String p = "%" + filtro + "%";
            for (int i = 1; i <= 5; i++) ps.setString(i, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error buscando: " + e.getMessage());
        }
        return lista;
    }

    private static Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("ID_cliente"));
        c.setCodigo(rs.getString("Codigo"));
        c.setNombre(rs.getString("Nombre"));
        c.setApellido(rs.getString("Apellido"));
        c.setCedula(rs.getString("Cedula") != null ? rs.getString("Cedula") : "");
        c.setTelefono(rs.getString("Telefono") != null ? rs.getString("Telefono") : "");
        c.setDireccion(rs.getString("Direccion") != null ? rs.getString("Direccion") : "");
        c.setEmail(rs.getString("Email") != null ? rs.getString("Email") : "");
        c.setTipoCliente(rs.getString("Tipo_cliente") != null ? rs.getString("Tipo_cliente") : "Regular");
        c.setEstado(rs.getString("Estado") != null ? rs.getString("Estado") : "Activo");
        c.setObservaciones(rs.getString("Observaciones") != null ? rs.getString("Observaciones") : "");
        Date fc = rs.getDate("fecha_creacion");
        c.setFechaCreacion(fc != null ? fc.toLocalDate() : null);
        Date uc = rs.getDate("ultima_compra");
        c.setUltimaCompra(uc != null ? uc.toLocalDate() : null);
        c.setTotalCompras(rs.getDouble("total_compras"));
        c.setCantidadCompras(rs.getInt("cantidad_compras"));
        return c;
    }

    // ── DIRECCIONES ─────────────────────────────────────────────────

    public static int guardarDireccion(int idCliente, String direccion, String referencia, String zona, String telefono) {
        String sql = "INSERT INTO tbl_DIRECCION_CLIENTE (fk_ID_cliente, Direccion, Referencia, Zona, Telefono) OUTPUT INSERTED.ID_direccion VALUES (?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, idCliente); ps.setString(2, direccion);
            ps.setString(3, referencia.isEmpty()?null:referencia);
            ps.setString(4, zona.isEmpty()?null:zona);
            ps.setString(5, telefono.isEmpty()?null:telefono);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error guardando dirección: " + e.getMessage());
            return -1;
        }
    }

    public static boolean actualizarDireccion(int id, String direccion, String referencia, String zona, String telefono) {
        String sql = "UPDATE tbl_DIRECCION_CLIENTE SET Direccion=?, Referencia=?, Zona=?, Telefono=? WHERE ID_direccion=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, direccion); ps.setString(2, referencia.isEmpty()?null:referencia);
            ps.setString(3, zona.isEmpty()?null:zona); ps.setString(4, telefono.isEmpty()?null:telefono);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error actualizando dirección: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarDireccion(int id) {
        String sql = "DELETE FROM tbl_DIRECCION_CLIENTE WHERE ID_direccion=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error eliminando dirección: " + e.getMessage());
            return false;
        }
    }

    public static List<Map<String, Object>> listarDirecciones() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT d.ID_direccion, c.Nombre + ' ' + c.Apellido AS Cliente, d.Direccion, ISNULL(d.Referencia,'') AS Referencia, ISNULL(d.Zona,'') AS Zona, ISNULL(d.Telefono,'') AS Telefono FROM tbl_DIRECCION_CLIENTE d JOIN tbl_CLIENTE c ON d.fk_ID_cliente=c.ID_cliente ORDER BY d.ID_direccion DESC";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return lista;
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("ID_direccion", rs.getInt("ID_direccion"));
                m.put("Cliente", rs.getString("Cliente"));
                m.put("Direccion", rs.getString("Direccion"));
                m.put("Referencia", rs.getString("Referencia"));
                m.put("Zona", rs.getString("Zona"));
                m.put("Telefono", rs.getString("Telefono"));
                lista.add(m);
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error listando direcciones: " + e.getMessage());
        }
        return lista;
    }

    // ── RESERVAS ────────────────────────────────────────────────────

    public static int guardarReserva(int idCliente, LocalDate fecha, String hora, int personas, String estado, String obs) {
        crearTablasSiNoExisten();
        String codigo = generarCodigoReserva();
        String sql = "INSERT INTO tbl_RESERVA_CLIENTE (Codigo, fk_ID_cliente, Fecha, Hora, Cantidad_personas, Estado, Observaciones) OUTPUT INSERTED.ID_reserva VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo); ps.setInt(2, idCliente);
            ps.setDate(3, Date.valueOf(fecha)); ps.setString(4, hora);
            ps.setInt(5, personas); ps.setString(6, estado);
            ps.setString(7, obs.isEmpty()?null:obs);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error guardando reserva: " + e.getMessage());
            return -1;
        }
    }

    public static boolean actualizarReserva(int id, LocalDate fecha, String hora, int personas, String estado, String obs) {
        String sql = "UPDATE tbl_RESERVA_CLIENTE SET Fecha=?, Hora=?, Cantidad_personas=?, Estado=?, Observaciones=? WHERE ID_reserva=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setDate(1, Date.valueOf(fecha)); ps.setString(2, hora);
            ps.setInt(3, personas); ps.setString(4, estado);
            ps.setString(5, obs.isEmpty()?null:obs); ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error actualizando reserva: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarReserva(int id) {
        String sql = "DELETE FROM tbl_RESERVA_CLIENTE WHERE ID_reserva=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error eliminando reserva: " + e.getMessage());
            return false;
        }
    }

    public static List<Map<String, Object>> listarReservas() {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT r.ID_reserva, r.Codigo, c.Nombre + ' ' + c.Apellido AS Cliente, r.Fecha, r.Hora, r.Cantidad_personas, r.Estado, ISNULL(r.Observaciones,'') AS Observaciones FROM tbl_RESERVA_CLIENTE r JOIN tbl_CLIENTE c ON r.fk_ID_cliente=c.ID_cliente ORDER BY r.Fecha DESC, r.Hora DESC";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return lista;
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("ID_reserva", rs.getInt("ID_reserva"));
                m.put("Codigo", rs.getString("Codigo"));
                m.put("Cliente", rs.getString("Cliente"));
                m.put("Fecha", rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : "");
                m.put("Hora", rs.getString("Hora"));
                m.put("Personas", rs.getInt("Cantidad_personas"));
                m.put("Estado", rs.getString("Estado"));
                m.put("Observaciones", rs.getString("Observaciones"));
                lista.add(m);
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error listando reservas: " + e.getMessage());
        }
        return lista;
    }

    // ── HISTORIAL COMPRAS ────────────────────────────────────────────

    public static List<Map<String, Object>> listarHistorialCompras(int idCliente) {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT ID_compra, Fecha, Numero_pedido, Total, ISNULL(Metodo_pago,'') AS Metodo_pago, Estado_pedido FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=? ORDER BY Fecha DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return lista;
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("ID_compra", rs.getInt("ID_compra"));
                    m.put("Fecha", rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : "");
                    m.put("Numero_pedido", rs.getString("Numero_pedido"));
                    m.put("Total", rs.getDouble("Total"));
                    m.put("Metodo_pago", rs.getString("Metodo_pago"));
                    m.put("Estado_pedido", rs.getString("Estado_pedido"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error historial: " + e.getMessage());
        }
        return lista;
    }

    // ── CLIENTES FRECUENTES ─────────────────────────────────────────

    public static List<Cliente> listarClientesFrecuentes() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT TOP 20 ID_cliente, Codigo, Nombre, Apellido, ISNULL(Cedula,'') AS Cedula, ISNULL(Telefono,'') AS Telefono, ISNULL(Direccion,'') AS Direccion, ISNULL(Email,'') AS Email, ISNULL(Tipo_cliente,'Regular') AS Tipo_cliente, ISNULL(Estado,'Activo') AS Estado, ISNULL(Observaciones,'') AS Observaciones, fecha_creacion, ultima_compra, ISNULL(total_compras,0) AS total_compras, ISNULL(cantidad_compras,0) AS cantidad_compras FROM tbl_CLIENTE WHERE cantidad_compras > 0 ORDER BY cantidad_compras DESC, total_compras DESC";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return lista;
            while (rs.next()) lista.add(mapearCliente(rs));
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error frecuentes: " + e.getMessage());
        }
        return lista;
    }

    // ── COMPRAS CLIENTE ─────────────────────────────────────────────

    public static int guardarCompraCliente(int idCliente, LocalDate fecha, String numPedido, double total, String metodo, String estado) {
        String sql = "INSERT INTO tbl_COMPRA_CLIENTE (fk_ID_cliente, Fecha, Numero_pedido, Total, Metodo_pago, Estado_pedido) OUTPUT INSERTED.ID_compra VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, idCliente); ps.setDate(2, Date.valueOf(fecha));
            ps.setString(3, numPedido); ps.setDouble(4, total);
            ps.setString(5, metodo.isEmpty()?null:metodo); ps.setString(6, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                actualizarResumenCliente(idCliente, conn);
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error guardando compra: " + e.getMessage());
            return -1;
        }
    }

    private static void actualizarResumenCliente(int idCliente, Connection conn) {
        String sql = "UPDATE tbl_CLIENTE SET cantidad_compras=(SELECT COUNT(*) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=?), total_compras=(SELECT ISNULL(SUM(Total),0) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=?), ultima_compra=(SELECT MAX(Fecha) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=?) WHERE ID_cliente=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente); ps.setInt(2, idCliente);
            ps.setInt(3, idCliente); ps.setInt(4, idCliente);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error actualizando resumen: " + e.getMessage());
        }
    }

    // ── SEED DATA ───────────────────────────────────────────────────

    public static void insertarSeedData() {
        crearTablasSiNoExisten();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            // Check if seed already done
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM tbl_CLIENTE WHERE Codigo IS NOT NULL")) {
                if (rs.next() && rs.getInt(1) >= 10) return;
            }
            // Clear seed-only tables (no FK constraints from old tables)
            try (Statement st = conn.createStatement()) { st.executeUpdate("DELETE FROM tbl_DIRECCION_CLIENTE"); }
            try (Statement st = conn.createStatement()) { st.executeUpdate("DELETE FROM tbl_RESERVA_CLIENTE"); }
            try (Statement st = conn.createStatement()) { st.executeUpdate("DELETE FROM tbl_COMPRA_CLIENTE"); }
            try (Statement st = conn.createStatement()) { st.executeUpdate("UPDATE tbl_CONTADOR_CLIENTE SET ultimo_numero=0"); }

            // UPDATE existing old rows that have Codigo=NULL with football data
            String[][] oldUpdates = {
                {"CLI-0001","Lionel","Messi","001-0000001-1","809-111-0001","Av. Messi 10, Miami","messi@email.com","VIP","Activo","El mejor del mundo"},
                {"CLI-0002","Cristiano","Ronaldo","001-0000002-2","809-111-0002","Av. CR7 7, Madeira","cristiano@email.com","VIP","Activo","GOAT discussion"},
                {"CLI-0003","Neymar","Jr","001-0000003-3","809-111-0003","Calle Santos 11","neymarjr@email.com","VIP","Activo","Jugador brasileño"},
                {"CLI-0004","Kylian","Mbappé","001-0000004-4","809-111-0004","Av. Francia 7","mbappe@email.com","VIP","Activo","Velocidad pura"},
                {"CLI-0005","Erling","Haaland","001-0000005-5","809-111-0005","Calle Noruega 9","haaland@email.com","Regular","Activo","Goleador nato"}
            };
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tbl_CLIENTE SET Codigo=?, Nombre=?, Apellido=?, Cedula=?, Telefono=?, Direccion=?, Email=?, Tipo_cliente=?, Estado=?, Observaciones=? WHERE ID_cliente=? AND Codigo IS NULL")) {
                for (int i = 0; i < oldUpdates.length; i++) {
                    String[] c = oldUpdates[i];
                    ps.setString(1, c[0]); ps.setString(2, c[1]); ps.setString(3, c[2]);
                    ps.setString(4, c[3]); ps.setString(5, c[4]); ps.setString(6, c[5]);
                    ps.setString(7, c[6]); ps.setString(8, c[7]); ps.setString(9, c[8]); ps.setString(10, c[9]);
                    ps.setInt(11, i+1);
                    ps.executeUpdate();
                }
            }
            // Get current max ID to know where to start inserting
            int maxId = 5;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT ISNULL(MAX(ID_cliente),0) FROM tbl_CLIENTE")) {
                if (rs.next()) maxId = Math.max(maxId, rs.getInt(1));
            }
            // INSERT remaining football players (6-15)
            String[][] newClients = {
                {"CLI-0006","Luka","Modrić","001-0000006-6","809-111-0006","Av. Croacia 10","modric@email.com","Regular","Activo","Balón de Oro 2018"},
                {"CLI-0007","Vinícius","Jr","001-0000007-7","809-111-0007","Calle Brasil 20","vini@email.com","VIP","Activo","Talento brasileño"},
                {"CLI-0008","Mohamed","Salah","001-0000008-8","809-111-0008","Av. Egipto 11","salah@email.com","VIP","Activo","Rey de Liverpool"},
                {"CLI-0009","Robert","Lewandowski","001-0000009-9","809-111-0009","Calle Polonia 9","lewy@email.com","Regular","Activo","Goleador polaco"},
                {"CLI-0010","Kevin","De Bruyne","001-0000010-0","809-111-0010","Av. Bélgica 17","kdb@email.com","VIP","Activo","Asistente estrella"},
                {"CLI-0011","Jude","Bellingham","001-0000011-1","809-111-0011","Calle Inglaterra 5","bellingham@email.com","Regular","Activo","Joven promesa"},
                {"CLI-0012","Vinicius","Tobias","001-0000012-2","809-111-0012","Av. Argentina 8","vtobias@email.com","Regular","Inactivo","Lesionado"},
                {"CLI-0013","Antoine","Griezmann","001-0000013-3","809-111-0013","Calle Francia 7","griezmann@email.com","Regular","Activo","Campeón del mundo"},
                {"CLI-0014","Harry","Kane","001-0000014-4","809-111-0014","Av. Inglaterra 10","hkane@email.com","Corporativo","Activo","Capitán inglês"},
                {"CLI-0015","Rafael","Leão","001-0000015-5","809-111-0015","Calle Portugal 17","rafael@email.com","Regular","Activo","Extremo veloz"}
            };
            try (PreparedStatement ps = conn.prepareStatement("IF NOT EXISTS (SELECT 1 FROM tbl_CLIENTE WHERE Codigo=?) INSERT INTO tbl_CLIENTE (Codigo,Nombre,Apellido,Cedula,Telefono,Direccion,Email,Tipo_cliente,Estado,Observaciones) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                for (String[] c : newClients) {
                    ps.setString(1, c[0]);
                    ps.setString(2, c[0]); ps.setString(3, c[1]); ps.setString(4, c[2]);
                    ps.setString(5, c[3]); ps.setString(6, c[4]); ps.setString(7, c[5]);
                    ps.setString(8, c[6]); ps.setString(9, c[7]); ps.setString(10, c[8]); ps.setString(11, c[9]);
                    ps.executeUpdate();
                }
            }
            // Map ID by Codigo for FK references
            java.util.Map<String,Integer> idMap = new java.util.HashMap<>();
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT ID_cliente, Codigo FROM tbl_CLIENTE WHERE Codigo IS NOT NULL")) {
                while (rs.next()) idMap.put(rs.getString("Codigo"), rs.getInt("ID_cliente"));
            }
            // Seed addresses
            String[][] direcciones = {
                {"CLI-0001","Av. Messi 10, Miami Beach","Cerca del estadio DRV PNK","Miami","809-111-0001"},
                {"CLI-0001","Calle Argentina 123, Buenos Aires","Edif. Albiceleste, Piso 5","Palermo","809-111-0101"},
                {"CLI-0002","Av. CR7 7, Funchal","Villa privada con vista al mar","Madeira","809-111-0002"},
                {"CLI-0003","Calle Santos 11, São Paulo","Cerca del estadio Vila Belmiro","Santos","809-111-0003"},
                {"CLI-0004","Av. Francia 7, París","Torre Eiffel, Distrito 7","Paris","809-111-0004"},
                {"CLI-0005","Calle Noruega 9, Oslo","Frente al fiordo","Oslo","809-111-0005"},
                {"CLI-0007","Calle Brasil 20, Río de Janeiro","Frente a Copacabana","Copacabana","809-111-0007"},
                {"CLI-0008","Av. Egipto 11, El Cairo","Cerca de las pirámides","Guiza","809-111-0008"},
                {"CLI-0009","Calle Polonia 9, Varsovia","Centro histórico","Warszawa","809-111-0009"},
                {"CLI-0014","Av. Londres 10","Zona residencial","London","809-111-0014"}
            };
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tbl_DIRECCION_CLIENTE (fk_ID_cliente,Direccion,Referencia,Zona,Telefono) VALUES (?,?,?,?,?)")) {
                for (String[] d : direcciones) { ps.setInt(1,idMap.getOrDefault(d[0],1)); ps.setString(2,d[1]); ps.setString(3,d[2]); ps.setString(4,d[3]); ps.setString(5,d[4]); ps.executeUpdate(); }
            }
            // Seed reservations
            String[][] reservas = {
                {"CLI-0001","2025-06-15","20:00","6","Confirmada","Cena especial con la familia"},
                {"CLI-0002","2025-06-16","21:00","8","Confirmada","Celebración título"},
                {"CLI-0003","2025-06-18","19:30","4","Confirmada","Reunión con amigos"},
                {"CLI-0004","2025-06-20","20:30","10","Confirmada","Fiesta de cumpleaños"},
                {"CLI-0005","2025-06-22","18:00","2","Pendiente","Cena romántica"},
                {"CLI-0006","2025-06-25","19:00","3","Confirmada","Despedida compañero"},
                {"CLI-0007","2025-06-28","21:30","12","Pendiente","Celebración gol 100"},
                {"CLI-0008","2025-06-30","20:00","5","Confirmada","Cena con la directiva"},
                {"CLI-0009","2025-07-02","19:00","2","Pendiente","Aniversario"},
                {"CLI-0010","2025-07-05","18:30","4","Cancelada","Cambio de agenda"}
            };
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tbl_RESERVA_CLIENTE (Codigo,fk_ID_cliente,Fecha,Hora,Cantidad_personas,Estado,Observaciones) VALUES (?,?,?,?,?,?,?)")) {
                int i = 1;
                for (String[] r : reservas) {
                    ps.setString(1, String.format("RES-%04d", i++));
                    ps.setInt(2, idMap.getOrDefault(r[0],1));
                    ps.setDate(3, Date.valueOf(r[1])); ps.setString(4, r[2]);
                    ps.setInt(5, Integer.parseInt(r[3])); ps.setString(6, r[4]);
                    ps.setString(7, r[5].isEmpty()?null:r[5]);
                    ps.executeUpdate();
                }
            }
            // Seed purchase history
            String[][] compras = {
                {"CLI-0001","2025-05-01","PED-001","12000.00","Tarjeta","Completado"},
                {"CLI-0001","2025-05-10","PED-002","8500.00","Tarjeta","Completado"},
                {"CLI-0001","2025-05-18","PED-003","15000.00","Efectivo","Completado"},
                {"CLI-0002","2025-04-20","PED-004","22000.00","Transferencia","Completado"},
                {"CLI-0002","2025-05-05","PED-005","18000.00","Tarjeta","Completado"},
                {"CLI-0002","2025-05-19","PED-006","9500.00","Efectivo","Completado"},
                {"CLI-0003","2025-05-02","PED-007","7500.00","Tarjeta","Completado"},
                {"CLI-0003","2025-05-15","PED-008","11000.00","Efectivo","Completado"},
                {"CLI-0004","2025-03-28","PED-009","28000.00","Tarjeta","Completado"},
                {"CLI-0004","2025-04-30","PED-010","32000.00","Transferencia","Completado"},
                {"CLI-0004","2025-05-17","PED-011","14500.00","Tarjeta","Completado"},
                {"CLI-0005","2025-04-10","PED-012","6500.00","Efectivo","Completado"},
                {"CLI-0005","2025-05-12","PED-013","8900.00","Tarjeta","Completado"},
                {"CLI-0006","2025-05-08","PED-014","5200.00","Efectivo","Completado"},
                {"CLI-0007","2025-04-15","PED-015","19000.00","Tarjeta","Completado"},
                {"CLI-0007","2025-05-01","PED-016","23000.00","Tarjeta","Completado"},
                {"CLI-0008","2025-03-20","PED-017","35000.00","Transferencia","Completado"},
                {"CLI-0008","2025-04-25","PED-018","28000.00","Tarjeta","Completado"},
                {"CLI-0008","2025-05-14","PED-019","16200.00","Efectivo","Completado"},
                {"CLI-0009","2025-04-05","PED-020","7800.00","Tarjeta","Completado"},
                {"CLI-0009","2025-05-09","PED-021","10500.00","Efectivo","Completado"},
                {"CLI-0010","2025-05-03","PED-022","4200.00","Tarjeta","Completado"},
                {"CLI-0010","2025-05-16","PED-023","6800.00","Efectivo","Completado"},
                {"CLI-0011","2025-05-11","PED-024","3400.00","Tarjeta","Completado"},
                {"CLI-0014","2025-04-22","PED-025","9500.00","Transferencia","Completado"}
            };
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO tbl_COMPRA_CLIENTE (fk_ID_cliente,Fecha,Numero_pedido,Total,Metodo_pago,Estado_pedido) VALUES (?,?,?,?,?,?)")) {
                for (String[] co : compras) { ps.setInt(1,idMap.getOrDefault(co[0],1)); ps.setDate(2,Date.valueOf(co[1])); ps.setString(3,co[2]); ps.setDouble(4,Double.parseDouble(co[3])); ps.setString(5,co[4]); ps.setString(6,co[5]); ps.executeUpdate(); }
            }
            // Update summary for all clients
            try (Statement stmt = conn.createStatement()) { stmt.executeUpdate("UPDATE c SET cantidad_compras=(SELECT COUNT(*) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=c.ID_cliente), total_compras=(SELECT ISNULL(SUM(Total),0) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=c.ID_cliente), ultima_compra=(SELECT MAX(Fecha) FROM tbl_COMPRA_CLIENTE WHERE fk_ID_cliente=c.ID_cliente) FROM tbl_CLIENTE c"); }
            System.out.println("[ClienteDAO] Seed data inserted successfully.");
        } catch (SQLException e) {
            System.out.println("[ClienteDAO] Error seed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
