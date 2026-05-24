package org.example.salsiaopf.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestión de conexión a SQL Server.
 * <p>
 * Los parámetros de conexión se leen desde
 * {@code /org/example/salsiaopf/database.properties}.
 * Si el archivo no existe o está incompleto, se usan valores por defecto
 * que coinciden con el entorno de desarrollo original.
 */
public class ConexionBD {

    private static final String PROPS_PATH = "/org/example/salsiaopf/database.properties";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        cargarPropiedades();
    }

    private static void cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getResourceAsStream(PROPS_PATH)) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("[ConexionBD] No se encontr\u00f3 " + PROPS_PATH
                        + ", usando valores por defecto.");
            }
        } catch (IOException e) {
            System.out.println("[ConexionBD] Error al leer " + PROPS_PATH + ": " + e.getMessage());
        }

        dbUrl = props.getProperty("db.url",
                "jdbc:sqlserver://MSIcyborg:1433;databaseName=SALSIAOREF;encrypt=true;trustServerCertificate=true");
        dbUser = props.getProperty("db.user", "fernando");
        dbPassword = props.getProperty("db.password", "123456");
    }

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            System.out.println("\u2705 Conexi\u00f3n exitosa a la base de datos SALSIAO");
            return conn;

        } catch (SQLException e) {
            System.out.println("\u274c Error al conectar a la base de datos:");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println("C\u00f3digo: " + e.getErrorCode());
            return null;
        }
    }
}