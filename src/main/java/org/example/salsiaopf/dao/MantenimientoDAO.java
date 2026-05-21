package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MantenimientoDAO {
    public static boolean guardarMantenimiento(String equipo, String descripcion) {
        String sql = "INSERT INTO tbl_MANTENIMIENTO (equipo, descripcion) VALUES (?, ?)";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipo);
            ps.setString(2, descripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardando mantenimiento: " + e.getMessage());
            return false;
        }
    }
}
