package org.example.salsiaopf.dao;

import org.mindrot.jbcrypt.BCrypt;
import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Usuario;

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
        // Asegurarse de que la tabla exista antes de consultar
        crearTablaSiNoExiste();

        // Buscar el usuario en la BD
        String sql = "SELECT id, nombre, usuario, contrasena, rol, estado "
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
                        return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("usuario"),
                            hashGuardado,
                            rs.getString("rol"),
                            rs.getString("estado"),
                            null // No guardamos la fecha de creación en este nuevo esquema para simplificar
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
     * Crea la tabla Usuarios si no existe.
     */
    public static void crearTablaSiNoExiste() {
        String sql = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Usuarios')
            BEGIN
                CREATE TABLE Usuarios (
                    id           INT IDENTITY(1,1) PRIMARY KEY,
                    nombre       VARCHAR(100)  NOT NULL,
                    usuario      VARCHAR(150)  NOT NULL UNIQUE,
                    contrasena   VARCHAR(255)  NOT NULL,
                    rol          VARCHAR(50)   NOT NULL,
                    estado       VARCHAR(20)   NOT NULL DEFAULT 'Activo'
                );
            END
            """;
        try (Connection conn = ConexionBD.conectar()) {
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            System.out.println("[UsuarioDAO] Error creando tabla Usuarios: " + e.getMessage());
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
