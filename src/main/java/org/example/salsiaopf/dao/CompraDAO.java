package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    public static boolean guardarCompra(String proveedor, double total) {
        String sql = """
                INSERT INTO tbl_COMPRA (proveedor, total)
                VALUES (?, ?)
                """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            ps.setString(1, proveedor);
            ps.setDouble(2, total);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardando compra: " + e.getMessage());
            return false;
        }
    }

    public static List<String[]> listarResumen() {
        List<String[]> filas = new ArrayList<>();
        String sql = """
                SELECT ID_compra, proveedor, total
                FROM tbl_COMPRA
                ORDER BY ID_compra DESC
                """;

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn == null) return filas;

            while (rs.next()) {
                filas.add(new String[]{
                        String.valueOf(rs.getInt("ID_compra")),
                        rs.getString("proveedor"),
                        String.format("RD$ %,.2f", rs.getDouble("total"))
                });
            }
        } catch (SQLException e) {
            System.out.println("Error listando compras: " + e.getMessage());
        }
        return filas;
    }
}
