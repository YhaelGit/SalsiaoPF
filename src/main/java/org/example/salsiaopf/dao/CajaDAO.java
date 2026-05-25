package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Caja;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CajaDAO {

    private static final String SQL_CREAR = """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'tbl_CAJA')
            BEGIN
                CREATE TABLE tbl_CAJA (
                    id_caja          INT IDENTITY(1,1) PRIMARY KEY,
                    fecha_apertura   DATETIME       NOT NULL DEFAULT GETDATE(),
                    fecha_cierre     DATETIME       NULL,
                    monto_inicial    DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    total_efectivo   DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    total_tarjeta    DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    total_transferencia DECIMAL(12,2) NOT NULL DEFAULT 0,
                    total_general    DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    diferencia       DECIMAL(12,2)  NOT NULL DEFAULT 0,
                    usuario          VARCHAR(100)   NOT NULL,
                    estado           VARCHAR(20)    NOT NULL DEFAULT 'Abierta'
                );
            END
            """;

    private CajaDAO() {}

    public static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement st = conn.createStatement()) {
                st.execute(SQL_CREAR);
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error creando tabla: " + e.getMessage());
        }
    }

    public static int abrirCaja(double montoInicial, String usuario) {
        String sql = "INSERT INTO tbl_CAJA (fecha_apertura, monto_inicial, usuario, estado) VALUES (GETDATE(), ?, ?, 'Abierta')";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return -1;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setDouble(1, montoInicial);
                ps.setString(2, usuario);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("SELECT SCOPE_IDENTITY()")) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error abriendo caja: " + e.getMessage());
        }
        return -1;
    }

    public static boolean cerrarCaja(int idCaja, double totalEfectivo, double totalTarjeta,
                                      double totalTransferencia, double totalGeneral, double diferencia) {
        String sql = "UPDATE tbl_CAJA SET fecha_cierre = GETDATE(), total_efectivo = ?, total_tarjeta = ?, " +
                     "total_transferencia = ?, total_general = ?, diferencia = ?, estado = 'Cerrada' WHERE id_caja = ?";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, totalEfectivo);
                ps.setDouble(2, totalTarjeta);
                ps.setDouble(3, totalTransferencia);
                ps.setDouble(4, totalGeneral);
                ps.setDouble(5, diferencia);
                ps.setInt(6, idCaja);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error cerrando caja: " + e.getMessage());
        }
        return false;
    }

    public static boolean hayCajaAbierta() {
        String sql = "SELECT COUNT(*) FROM tbl_CAJA WHERE estado = 'Abierta'";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return false;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error verificando caja abierta: " + e.getMessage());
        }
        return false;
    }

    public static Caja obtenerCajaAbierta() {
        String sql = "SELECT TOP 1 * FROM tbl_CAJA WHERE estado = 'Abierta' ORDER BY id_caja DESC";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return null;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error obteniendo caja abierta: " + e.getMessage());
        }
        return null;
    }

    public static List<Caja> listarCierres() {
        List<Caja> lista = new ArrayList<>();
        String sql = "SELECT * FROM tbl_CAJA WHERE estado = 'Cerrada' ORDER BY fecha_cierre DESC";
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return lista;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("[CajaDAO] Error listando cierres: " + e.getMessage());
        }
        return lista;
    }

    private static Caja mapear(ResultSet rs) throws SQLException {
        Caja c = new Caja();
        c.setIdCaja(rs.getInt("id_caja"));
        c.setFechaApertura(rs.getTimestamp("fecha_apertura").toLocalDateTime());
        Timestamp fc = rs.getTimestamp("fecha_cierre");
        if (fc != null) c.setFechaCierre(fc.toLocalDateTime());
        c.setMontoInicial(rs.getDouble("monto_inicial"));
        c.setTotalEfectivo(rs.getDouble("total_efectivo"));
        c.setTotalTarjeta(rs.getDouble("total_tarjeta"));
        c.setTotalTransferencia(rs.getDouble("total_transferencia"));
        c.setTotalGeneral(rs.getDouble("total_general"));
        c.setDiferencia(rs.getDouble("diferencia"));
        c.setUsuario(rs.getString("usuario"));
        c.setEstado(rs.getString("estado"));
        return c;
    }
}
