package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VentaDAO {
    public static boolean guardarVenta(int idCliente, double total) {
        String sql = "INSERT INTO tbl_VENTA (ID_cliente, total) VALUES (?, ?)";
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setDouble(2, total);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardando venta: " + e.getMessage());
            return false;
        }
    }
}
