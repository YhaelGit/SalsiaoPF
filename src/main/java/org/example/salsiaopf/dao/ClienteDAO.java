package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    public boolean guardar(Cliente cliente) {

        String sql = """
                INSERT INTO tbl_CLIENTE
                (nombre, apellido, telefono)
                VALUES (?, ?, ?)
                """;

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getTelefono());

            ps.executeUpdate();

            return true;

        } catch (Exception e) {

            System.out.println("Error guardando cliente: " + e.getMessage());

            return false;
        }
    }

    public List<Cliente> listar() {

        List<Cliente> lista = new ArrayList<>();

        String sql = """
                SELECT *
                FROM tbl_CLIENTE
                ORDER BY ID_cliente DESC
                """;

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Cliente cliente = new Cliente();

                cliente.setIdCliente(
                        rs.getInt("ID_cliente"));

                cliente.setNombre(
                        rs.getString("nombre"));

                cliente.setApellido(
                        rs.getString("apellido"));

                cliente.setTelefono(
                        rs.getString("telefono"));

                lista.add(cliente);
            }

        } catch (Exception e) {

            System.out.println("Error listando clientes: " + e.getMessage());
        }

        return lista;
    }

    public boolean guardarDireccion(String direccion) {
        String sql = """
                INSERT INTO tbl_DIRECCION (nombre, fk_ID_cliente)
                VALUES (?, IDENT_CURRENT('tbl_CLIENTE'))
                """;

        try (
                Connection con = ConexionBD.conectar();
                PreparedStatement ps = con.prepareStatement(sql)) {

            if (con == null) return false;

            ps.setString(1, direccion);
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.out.println("Error guardando dirección: " + e.getMessage());
            return false;
        }
    }
}