package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public static void crearTablaSiNoExiste() {
        String sql = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_PROVEEDOR')
            BEGIN
                CREATE TABLE tbl_PROVEEDOR (
                    ID_proveedor INT IDENTITY(1,1) PRIMARY KEY,
                    Nombre       VARCHAR(50) NOT NULL,
                    Telefono     VARCHAR(20) NULL,
                    Direccion    VARCHAR(255) NULL,
                    RNC          VARCHAR(20) NULL,
                    Correo       VARCHAR(100) NULL
                );
            END
            """;
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println("[ProveedorDAO] Error creando tabla: " + e.getMessage());
        }
    }

    public static List<Object[]> listarProveedores() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT ID_proveedor, Nombre, Telefono, Direccion, RNC, ISNULL(Correo,'') AS Correo FROM tbl_PROVEEDOR ORDER BY Nombre";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_proveedor"),
                    rs.getString("Nombre"),
                    nz(rs.getString("Telefono")),
                    nz(rs.getString("Direccion")),
                    nz(rs.getString("RNC")),
                    nz(rs.getString("Correo"))
                });
            }
        } catch (SQLException e) {
            System.out.println("[ProveedorDAO] Error listando: " + e.getMessage());
        }
        return filas;
    }

    public static int guardarProveedor(String nombre, String telefono, String direccion, String rnc, String correo) {
        crearTablaSiNoExiste();
        String sql = "INSERT INTO tbl_PROVEEDOR (Nombre, Telefono, Direccion, RNC, Correo) OUTPUT INSERTED.ID_proveedor VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, nombre);
            ps.setString(2, telefono.isEmpty() ? null : telefono);
            ps.setString(3, direccion.isEmpty() ? null : direccion);
            ps.setString(4, rnc.isEmpty() ? null : rnc);
            ps.setString(5, correo.isEmpty() ? null : correo);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (SQLException e) {
            System.out.println("[ProveedorDAO] Error guardando: " + e.getMessage());
            return -1;
        }
    }

    public static boolean actualizarProveedor(int id, String nombre, String telefono, String direccion, String rnc, String correo) {
        String sql = "UPDATE tbl_PROVEEDOR SET Nombre=?, Telefono=?, Direccion=?, RNC=?, Correo=? WHERE ID_proveedor=?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre);
            ps.setString(2, telefono.isEmpty() ? null : telefono);
            ps.setString(3, direccion.isEmpty() ? null : direccion);
            ps.setString(4, rnc.isEmpty() ? null : rnc);
            ps.setString(5, correo.isEmpty() ? null : correo);
            ps.setInt(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ProveedorDAO] Error actualizando: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarProveedor(int id) {
        String sql = "DELETE FROM tbl_PROVEEDOR WHERE ID_proveedor = ?";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[ProveedorDAO] Error eliminando: " + e.getMessage());
            return false;
        }
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
