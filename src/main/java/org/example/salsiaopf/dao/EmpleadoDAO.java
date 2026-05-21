package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Empleado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public static boolean guardarEmpleado(String nombre, String apellido, String cedula,
                                          String telefono, String correo, String direccion) {
        String sql = """
                INSERT INTO tbl_EMPLEADO (nombre, apellido, cedula, telefono, correo, direccion)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (conn == null) return false;

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, cedula);
            ps.setString(4, telefono);
            ps.setString(5, correo);
            ps.setString(6, direccion);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error guardando empleado: " + e.getMessage());
            return false;
        }
    }

    public static List<Empleado> listar() {
        List<Empleado> lista = new ArrayList<>();
        String sql = """
                SELECT ID_empleado, nombre, apellido, cedula, telefono, correo, direccion
                FROM tbl_EMPLEADO
                ORDER BY ID_empleado DESC
                """;

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (conn == null) return lista;

            while (rs.next()) {
                Empleado e = new Empleado();
                e.setId(rs.getInt("ID_empleado"));
                e.setNombre(rs.getString("nombre"));
                e.setApellido(rs.getString("apellido"));
                e.setCedula(rs.getString("cedula"));
                e.setTelefono(rs.getString("telefono"));
                e.setCorreo(rs.getString("correo"));
                e.setDireccion(rs.getString("direccion"));
                lista.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error listando empleados: " + e.getMessage());
        }
        return lista;
    }
}
