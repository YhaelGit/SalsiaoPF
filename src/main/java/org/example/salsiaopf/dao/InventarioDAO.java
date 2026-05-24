package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    public static void crearTablasSiNoExisten() {
        String[] sqls = {
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_PRODUCTO') BEGIN CREATE TABLE tbl_PRODUCTO (ID_producto INT IDENTITY(1,1) PRIMARY KEY, Codigo VARCHAR(20) NOT NULL UNIQUE, Nombre VARCHAR(100) NOT NULL, Categoria VARCHAR(50) NULL, Unidad VARCHAR(10) NOT NULL DEFAULT 'UN', Cantidad DECIMAL(10,2) NOT NULL DEFAULT 0, Costo DECIMAL(10,2) NOT NULL DEFAULT 0, Stock_minimo DECIMAL(10,2) NOT NULL DEFAULT 5, Estado VARCHAR(20) NOT NULL DEFAULT 'Activo', Fecha_vencimiento DATE NULL, Observacion VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_MOVIMIENTO_INVENTARIO') BEGIN CREATE TABLE tbl_MOVIMIENTO_INVENTARIO (ID_movimiento INT IDENTITY(1,1) PRIMARY KEY, Tipo VARCHAR(20) NOT NULL, fk_ID_producto INT NOT NULL, Cantidad DECIMAL(10,2) NOT NULL, Fecha DATE NOT NULL, Observacion VARCHAR(500) NULL, Usuario VARCHAR(50) NULL, fecha_registro DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CONTADOR_PRODUCTO') BEGIN CREATE TABLE tbl_CONTADOR_PRODUCTO (ultimo_numero INT NOT NULL DEFAULT 0); INSERT INTO tbl_CONTADOR_PRODUCTO VALUES (0) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CATEGORIA_INVENTARIO') BEGIN CREATE TABLE tbl_CATEGORIA_INVENTARIO (ID_categoria INT IDENTITY(1,1) PRIMARY KEY, Nombre VARCHAR(50) NOT NULL UNIQUE, Descripcion VARCHAR(200) NULL) END"
        };
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                for (String s : sqls) stmt.execute(s);
            }
        } catch (SQLException e) {
            System.out.println("[InventarioDAO] Error creando tablas: " + e.getMessage());
        }
    }

    public static synchronized String generarCodigoProducto() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "PROD-0001";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE tbl_CONTADOR_PRODUCTO SET ultimo_numero = ultimo_numero + 1");
                ResultSet rs = stmt.executeQuery("SELECT ultimo_numero FROM tbl_CONTADOR_PRODUCTO");
                return rs.next() ? String.format("PROD-%04d", rs.getInt(1)) : "PROD-0001";
            }
        } catch (SQLException e) {
            return "PROD-" + System.currentTimeMillis() % 10000;
        }
    }

    // ── PRODUCTOS ──────────────────────────────────────────────────────

    public static int guardarProducto(String codigo, String nombre, String categoria, String unidad, double cantidad, double costo, double stockMin, String estado, LocalDate venc, String obs) {
        crearTablasSiNoExisten();
        String sql = "INSERT INTO tbl_PRODUCTO (Codigo, Nombre, Categoria, Unidad, Cantidad, Costo, Stock_minimo, Estado, Fecha_vencimiento, Observacion) OUTPUT INSERTED.ID_producto VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo); ps.setString(2, nombre); ps.setString(3, categoria.isEmpty()?null:categoria);
            ps.setString(4, unidad); ps.setDouble(5, cantidad); ps.setDouble(6, costo);
            ps.setDouble(7, stockMin); ps.setString(8, estado);
            ps.setDate(9, venc != null ? Date.valueOf(venc) : null);
            ps.setString(10, obs.isEmpty()?null:obs);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("[InventarioDAO] Error guardando producto: " + e.getMessage());
            return -1;
        }
    }

    public static boolean actualizarProducto(int id, String nombre, String categoria, String unidad, double cantidad, double costo, double stockMin, String estado, LocalDate venc, String obs) {
        String sql = "UPDATE tbl_PRODUCTO SET Nombre=?, Categoria=?, Unidad=?, Cantidad=?, Costo=?, Stock_minimo=?, Estado=?, Fecha_vencimiento=?, Observacion=? WHERE ID_producto=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre); ps.setString(2, categoria.isEmpty()?null:categoria);
            ps.setString(3, unidad); ps.setDouble(4, cantidad); ps.setDouble(5, costo);
            ps.setDouble(6, stockMin); ps.setString(7, estado);
            ps.setDate(8, venc != null ? Date.valueOf(venc) : null);
            ps.setString(9, obs.isEmpty()?null:obs); ps.setInt(10, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[InventarioDAO] Error actualizando: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarProducto(int id) {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM tbl_MOVIMIENTO_INVENTARIO WHERE fk_ID_producto=?"); PreparedStatement ps2 = conn.prepareStatement("DELETE FROM tbl_PRODUCTO WHERE ID_producto=?")) {
                ps1.setInt(1, id); ps1.executeUpdate();
                ps2.setInt(1, id); boolean ok = ps2.executeUpdate() > 0;
                conn.commit(); return ok;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) {
            System.out.println("[InventarioDAO] Error eliminando: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarProductos(String filtro) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT ID_producto, Codigo, Nombre, ISNULL(Categoria,'') AS Categoria, Unidad, Cantidad, Costo, Stock_minimo, Estado, Fecha_vencimiento, ISNULL(Observacion,'') AS Observacion FROM tbl_PRODUCTO";
        if (filtro != null && !filtro.isEmpty()) sql += " WHERE Nombre LIKE ? OR Codigo LIKE ? OR Categoria LIKE ?";
        sql += " ORDER BY Nombre";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return filas;
            if (filtro != null && !filtro.isEmpty()) {
                String p = "%" + filtro + "%";
                ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(toArray(rs));
            }
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error listando: " + e.getMessage()); }
        return filas;
    }

    public static List<Object[]> listarTodos() { return listarProductos(null); }

    public static Object[] obtenerProducto(int id) {
        String sql = "SELECT ID_producto, Codigo, Nombre, ISNULL(Categoria,'') AS Categoria, Unidad, Cantidad, Costo, Stock_minimo, Estado, Fecha_vencimiento, ISNULL(Observacion,'') AS Observacion FROM tbl_PRODUCTO WHERE ID_producto=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null; ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? toArray(rs) : null; }
        } catch (SQLException e) { return null; }
    }

    private static Object[] toArray(ResultSet rs) throws SQLException {
        return new Object[]{rs.getInt("ID_producto"), rs.getString("Codigo"), rs.getString("Nombre"), rs.getString("Categoria"), rs.getString("Unidad"), rs.getDouble("Cantidad"), rs.getDouble("Costo"), rs.getDouble("Stock_minimo"), rs.getString("Estado"), rs.getDate("Fecha_vencimiento") != null ? rs.getDate("Fecha_vencimiento").toLocalDate() : null, rs.getString("Observacion")};
    }

    public static List<Object[]> listarStockBajo() {
        List<Object[]> filas = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT ID_producto, Codigo, Nombre, Unidad, Cantidad, Stock_minimo FROM tbl_PRODUCTO WHERE Cantidad <= Stock_minimo AND Estado='Activo' ORDER BY Cantidad ASC")) {
            if (conn == null) return filas;
            while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDouble(6)});
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error stock bajo: " + e.getMessage()); }
        return filas;
    }

    public static List<Object[]> listarVencidos() {
        List<Object[]> filas = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("SELECT ID_producto, Codigo, Nombre, Categoria, Cantidad, Fecha_vencimiento FROM tbl_PRODUCTO WHERE Fecha_vencimiento IS NOT NULL AND Fecha_vencimiento <= ? ORDER BY Fecha_vencimiento")) {
            if (conn == null) return filas; ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDate(6).toLocalDate()}); }
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error vencidos: " + e.getMessage()); }
        return filas;
    }

    // ── MOVIMIENTOS ────────────────────────────────────────────────────

    public static boolean registrarMovimiento(String tipo, int fkProducto, double cantidad, LocalDate fecha, String obs, String usuario) {
        String sqlProd = "UPDATE tbl_PRODUCTO SET Cantidad = Cantidad + ? WHERE ID_producto=?";
        String sqlMov = "INSERT INTO tbl_MOVIMIENTO_INVENTARIO (Tipo, fk_ID_producto, Cantidad, Fecha, Observacion, Usuario) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlProd); PreparedStatement ps2 = conn.prepareStatement(sqlMov)) {
                double signo = "Entrada".equals(tipo) ? cantidad : -cantidad;
                ps1.setDouble(1, signo); ps1.setInt(2, fkProducto); ps1.executeUpdate();
                ps2.setString(1, tipo); ps2.setInt(2, fkProducto); ps2.setDouble(3, cantidad);
                ps2.setDate(4, Date.valueOf(fecha)); ps2.setString(5, obs.isEmpty()?null:obs); ps2.setString(6, usuario);
                ps2.executeUpdate();
                conn.commit(); return true;
            } catch (SQLException e) { conn.rollback(); throw e; }
        } catch (SQLException e) {
            System.out.println("[InventarioDAO] Error registro movimiento: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarMovimientos(String filtro) {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT m.ID_movimiento, m.Tipo, p.Codigo, p.Nombre, m.Cantidad, m.Fecha, ISNULL(m.Observacion,'') AS Obs, ISNULL(m.Usuario,'') AS Usuario FROM tbl_MOVIMIENTO_INVENTARIO m LEFT JOIN tbl_PRODUCTO p ON m.fk_ID_producto=p.ID_producto";
        if (filtro != null && !filtro.isEmpty()) sql += " WHERE p.Nombre LIKE ? OR p.Codigo LIKE ? OR m.Tipo LIKE ?";
        sql += " ORDER BY m.ID_movimiento DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return filas;
            if (filtro != null && !filtro.isEmpty()) { String p = "%"+filtro+"%"; ps.setString(1,p); ps.setString(2,p); ps.setString(3,p); }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5), rs.getDate(6) != null ? rs.getDate(6).toLocalDate() : null, rs.getString(7), rs.getString(8)});
            }
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error movimientos: " + e.getMessage()); }
        return filas;
    }

    // ── CATEGORÍAS ─────────────────────────────────────────────────────

    public static List<String> listarCategorias() {
        List<String> cats = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT Nombre FROM tbl_CATEGORIA_INVENTARIO ORDER BY Nombre")) {
            if (conn == null) return cats; while (rs.next()) cats.add(rs.getString(1));
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error categorias: " + e.getMessage()); }
        return cats;
    }

    public static List<Object[]> listarCategoriasFull() {
        List<Object[]> filas = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT c.ID_categoria, c.Nombre, ISNULL(c.Descripcion,'') AS Descripcion, (SELECT COUNT(*) FROM tbl_PRODUCTO p WHERE p.Categoria=c.Nombre) AS Total FROM tbl_CATEGORIA_INVENTARIO c ORDER BY c.Nombre")) {
            if (conn == null) return filas; while (rs.next()) filas.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4)});
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error categorias: " + e.getMessage()); }
        return filas;
    }

    public static boolean guardarCategoria(String nombre, String descripcion) {
        String sql = "INSERT INTO tbl_CATEGORIA_INVENTARIO (Nombre, Descripcion) VALUES (?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false; ps.setString(1, nombre); ps.setString(2, descripcion.isEmpty()?null:descripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[InventarioDAO] Error guardar categoria: " + e.getMessage()); return false; }
    }

    public static boolean eliminarCategoria(String nombre) {
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_CATEGORIA_INVENTARIO WHERE Nombre=?")) {
            if (conn == null) return false; ps.setString(1, nombre); return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // ── ESTADÍSTICAS ───────────────────────────────────────────────────

    public static Object[] obtenerResumen() {
        String sql = "SELECT COUNT(*) AS total, ISNULL(SUM(CASE WHEN Cantidad=0 THEN 1 ELSE 0 END),0) AS agotados, ISNULL(SUM(CASE WHEN Cantidad<=Stock_minimo AND Cantidad>0 THEN 1 ELSE 0 END),0) AS bajos, ISNULL(SUM(CASE WHEN Fecha_vencimiento IS NOT NULL AND Fecha_vencimiento<=GETDATE() THEN 1 ELSE 0 END),0) AS vencidos, ISNULL(SUM(Cantidad*Costo),0) AS valor_total FROM tbl_PRODUCTO";
        try (Connection conn = ConexionBD.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (conn == null) return new Object[]{0,0,0,0,0.0};
            return rs.next() ? new Object[]{rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getDouble(5)} : new Object[]{0,0,0,0,0.0};
        } catch (SQLException e) { return new Object[]{0,0,0,0,0.0}; }
    }
}
