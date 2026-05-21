package org.example.salsiaopf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:sqlserver://localhost:1433;instanceName=SQLEXPRESS;databaseName=SALSIAOREF;encrypt=true;trustServerCertificate=true";

    private static final String USER = "fernando";
    private static final String PASSWORD = "123456";

    public static Connection conectar() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Conexión exitosa a la base de datos SALSIAO");
            return conn;

        } catch (SQLException e) {
            System.out.println("❌ Error al conectar a la base de datos:");
            System.out.println("Mensaje: " + e.getMessage());
            System.out.println("Código: " + e.getErrorCode());
            return null;
        }
    }
}