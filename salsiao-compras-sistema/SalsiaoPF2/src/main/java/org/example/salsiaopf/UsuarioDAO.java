package org.example.salsiaopf;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object para la tabla Usuarios.
 * Centraliza toda la lógica de BD relacionada con autenticación.
 */
public class UsuarioDAO {

    /**
     * Autentica a un usuario verificando su nombre de usuario y contraseña contra la BD.
     *
     * @param usuario    Usuario ingresado en el formulario de login
     * @param contrasena Contraseña en texto plano ingresada por el usuario
     * @return El objeto Usuario si las credenciales son válidas, null en caso contrario
     */
    public static Usuario autenticar(String usuario, String contrasena) {
        // Buscar el usuario en la BD
        String sql = "SELECT id_usuario, nombre, usuario, contrasena, rol, estado, fecha_creacion "
                   + "FROM Usuarios "
                   + "WHERE usuario = ? AND estado = 'Activo'";

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usuario.trim().toLowerCase());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String hashGuardado = rs.getString("contrasena");

                    // ✅ Verificar con BCrypt. Si no es hash o falla, caemos en comparación directa (texto plano)
                    boolean esValido = false;
                    if (hashGuardado != null && (hashGuardado.startsWith("$2a$") || hashGuardado.startsWith("$2y$") || hashGuardado.startsWith("$2b$"))) {
                        try {
                            esValido = BCrypt.checkpw(contrasena, hashGuardado);
                        } catch (Exception e) {
                            esValido = contrasena.equals(hashGuardado);
                        }
                    } else {
                        esValido = contrasena.equals(hashGuardado);
                    }

                    if (esValido) {
                        LocalDateTime fechaCreacion = null;
                        Timestamp ts = rs.getTimestamp("fecha_creacion");
                        if (ts != null) {
                            fechaCreacion = ts.toLocalDateTime();
                        }

                        return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            hashGuardado,
                            rs.getString("rol"),
                            rs.getString("estado"),
                            fechaCreacion
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("[UsuarioDAO] Error de autenticación: " + e.getMessage());
        }

        return null;   // Credenciales incorrectas o error
    }

    /**
     * Verifica si hay conexión a la base de datos.
     * @return true si la conexión es exitosa
     */
    public static boolean verificarConexion() {
        try (Connection conn = ConexionBD.conectar()) {
            return conn != null;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Crea un hash BCrypt de la contraseña proporcionada.
     * Útil para insertar usuarios manualmente o desde un formulario de registro.
     *
     * @param contrasenaPlana Contraseña en texto plano
     * @return Hash BCrypt seguro (coste 12)
     */
    public static String hashContrasena(String contrasenaPlana) {
        return BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt(12));
    }
}
