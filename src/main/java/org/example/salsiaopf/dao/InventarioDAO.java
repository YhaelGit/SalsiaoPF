package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Ingrediente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventarioDAO {

    public static boolean guardarIngrediente(String nombre, double costo, int stock) {
        String sql = """
                INSERT INTO tbl_INVENTARIO (nombre, costo, stock)
                VALUES (?, ?, ?)
                """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            ps.setString(1, nombre);
            ps.setDouble(2, costo);
            ps.setInt(3, stock);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardando ingrediente: " + e.getMessage());
            return false;
        }
    }

    public static List<Ingrediente> listar() {
        List<Ingrediente> lista = new ArrayList<>();
        String sql = """
                SELECT ID_inventario, nombre, costo, stock
                FROM tbl_INVENTARIO
                ORDER BY ID_inventario DESC
                """;

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn == null) return lista;

            while (rs.next()) {
                Ingrediente ing = new Ingrediente();
                ing.setId(rs.getInt("ID_inventario"));
                ing.setNombre(rs.getString("nombre"));
                ing.setCosto(rs.getDouble("costo"));
                ing.setStock(rs.getInt("stock"));
                lista.add(ing);
            }
        } catch (SQLException e) {
            System.out.println("Error listando inventario: " + e.getMessage());
        }
        return lista;
    }
}
