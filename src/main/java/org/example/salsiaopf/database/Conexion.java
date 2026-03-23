package org.example.salsiaopf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String USUARIO = "Yhael";
    private static final String CONTRASENA = "123456789";
    private static final String DB = "Agenda";
    private static final String SERVER = "localhost";
    private static final String PUERTO = "1433";

    public static Connection conectar() {
        Connection connection = null;

        try {
            String url = "jdbc:sqlserver://" + SERVER + ":" + PUERTO + ";" +
                    "databaseName=" + DB + ";" +
                    "encrypt=true;" +
                    "trustServerCertificate=true;";

            connection = DriverManager.getConnection(url, USUARIO, CONTRASENA);

            System.out.println("✅ Conexión exitosa a la base de datos");

        } catch (SQLException e) {
            System.out.println("❌ Error en la conexión: " + e.getMessage());
        }

        return connection;
    }
}