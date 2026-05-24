package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoDAO {

    public static void crearTablasSiNoExisten() {
        String[] sqls = {
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_EQUIPO') BEGIN CREATE TABLE tbl_EQUIPO (ID_equipo INT IDENTITY(1,1) PRIMARY KEY, Codigo VARCHAR(20) NOT NULL UNIQUE, Nombre VARCHAR(100) NOT NULL, Area VARCHAR(100) NULL, Marca VARCHAR(100) NULL, Modelo VARCHAR(100) NULL, Fecha_compra DATE NULL, Estado VARCHAR(20) NOT NULL DEFAULT 'Bueno', Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_MANTENIMIENTO') BEGIN CREATE TABLE tbl_MANTENIMIENTO (ID_mantenimiento INT IDENTITY(1,1) PRIMARY KEY, Codigo VARCHAR(20) NOT NULL UNIQUE, fk_ID_equipo INT NOT NULL, Descripcion_problema VARCHAR(500) NOT NULL, Fecha_reporte DATE NOT NULL, Prioridad VARCHAR(20) NOT NULL DEFAULT 'Media', Persona_reporta VARCHAR(100) NULL, fk_ID_tecnico INT NULL, Estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente', Solucion_aplicada VARCHAR(500) NULL, Fecha_solucion DATE NULL, Tiempo_reparacion VARCHAR(50) NULL, Costo_piezas DECIMAL(10,2) DEFAULT 0, Costo_mano_obra DECIMAL(10,2) DEFAULT 0, Costo_total DECIMAL(10,2) DEFAULT 0, Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_TECNICO') BEGIN CREATE TABLE tbl_TECNICO (ID_tecnico INT IDENTITY(1,1) PRIMARY KEY, Nombre VARCHAR(100) NOT NULL, Telefono VARCHAR(20) NULL, Especialidad VARCHAR(100) NULL, Tipo VARCHAR(20) NOT NULL DEFAULT 'Interno', Empresa VARCHAR(100) NULL, Estado VARCHAR(20) NOT NULL DEFAULT 'Activo', Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CALENDARIO_MANTENIMIENTO') BEGIN CREATE TABLE tbl_CALENDARIO_MANTENIMIENTO (ID_calendario INT IDENTITY(1,1) PRIMARY KEY, fk_ID_equipo INT NOT NULL, Fecha_programada DATE NOT NULL, Tipo_mantenimiento VARCHAR(100) NOT NULL, fk_ID_tecnico INT NULL, Estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente', Observaciones VARCHAR(500) NULL, fecha_creacion DATETIME DEFAULT GETDATE()) END",
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CONTADOR_MANTENIMIENTO') BEGIN CREATE TABLE tbl_CONTADOR_MANTENIMIENTO (fecha DATE PRIMARY KEY, ultimo_numero INT NOT NULL DEFAULT 0) END"
        };
        for (String sql : sqls) {
            try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
                if (conn != null) ps.execute();
            } catch (SQLException e) {
                System.out.println("[MantenimientoDAO] Error creando tabla: " + e.getMessage());
            }
        }
    }

    private static String generarCodigo() {
        String fecha = java.time.LocalDate.now().toString();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "MT-0001";
            String upd = "UPDATE tbl_CONTADOR_MANTENIMIENTO SET ultimo_numero = ultimo_numero + 1 OUTPUT INSERTED.ultimo_numero WHERE fecha=?";
            try (PreparedStatement ps = conn.prepareStatement(upd)) {
                ps.setString(1, fecha);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return String.format("MT-%04d", rs.getInt(1));
            }
            String ins = "INSERT INTO tbl_CONTADOR_MANTENIMIENTO (fecha, ultimo_numero) VALUES (?, 1)";
            try (PreparedStatement ps2 = conn.prepareStatement(ins)) {
                ps2.setString(1, fecha);
                ps2.execute();
            }
            return "MT-0001";
        } catch (SQLException e) {
            System.out.println("[MantenimientoDAO] Error generando codigo: " + e.getMessage());
            return "MT-" + (int)(Math.random()*9000+1000);
        }
    }

    private static String generarCodigoEquipo() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "EQ-001";
            String sql = "SELECT ISNULL(MAX(ID_equipo),0)+1 AS n FROM tbl_EQUIPO";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return String.format("EQ-%03d", rs.getInt("n"));
            }
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error cod equipo: " + e.getMessage()); }
        return "EQ-001";
    }

    // ═══════════════════════════ EQUIPOS ═══════════════════════════

    public static List<Object[]> listarEquipos() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT ID_equipo, Codigo, Nombre, ISNULL(Area,'') AS Area, ISNULL(Marca,'') AS Marca, ISNULL(Modelo,'') AS Modelo, ISNULL(CAST(Fecha_compra AS VARCHAR),'') AS Fecha_compra, Estado, ISNULL(Observaciones,'') AS Observaciones FROM tbl_EQUIPO ORDER BY Nombre";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) filas.add(new Object[]{rs.getInt("ID_equipo"), rs.getString("Codigo"), rs.getString("Nombre"), rs.getString("Area"), rs.getString("Marca"), rs.getString("Modelo"), rs.getString("Fecha_compra"), rs.getString("Estado"), rs.getString("Observaciones")});
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error listar equipos: " + e.getMessage()); }
        return filas;
    }

    public static int guardarEquipo(String codigo, String nombre, String area, String marca, String modelo, String fechaCompra, String estado, String observaciones) {
        String sql = "INSERT INTO tbl_EQUIPO (Codigo, Nombre, Area, Marca, Modelo, Fecha_compra, Estado, Observaciones) OUTPUT INSERTED.ID_equipo VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo); ps.setString(2, nombre); ps.setString(3, area); ps.setString(4, marca); ps.setString(5, modelo);
            ps.setString(6, fechaCompra.isEmpty() ? null : fechaCompra); ps.setString(7, estado); ps.setString(8, observaciones);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error guardar equipo: " + e.getMessage()); }
        return -1;
    }

    public static boolean actualizarEquipo(int id, String codigo, String nombre, String area, String marca, String modelo, String fechaCompra, String estado, String observaciones) {
        String sql = "UPDATE tbl_EQUIPO SET Codigo=?, Nombre=?, Area=?, Marca=?, Modelo=?, Fecha_compra=?, Estado=?, Observaciones=? WHERE ID_equipo=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, codigo); ps.setString(2, nombre); ps.setString(3, area); ps.setString(4, marca); ps.setString(5, modelo);
            ps.setString(6, fechaCompra.isEmpty() ? null : fechaCompra); ps.setString(7, estado); ps.setString(8, observaciones); ps.setInt(9, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error actualizar equipo: " + e.getMessage()); }
        return false;
    }

    public static boolean eliminarEquipo(int id) {
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_EQUIPO WHERE ID_equipo=?")) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error eliminar equipo: " + e.getMessage()); }
        return false;
    }

    public static List<String> obtenerNombresEquipos() {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("SELECT ID_equipo, Nombre FROM tbl_EQUIPO ORDER BY Nombre"); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return nombres;
            while (rs.next()) nombres.add(rs.getInt("ID_equipo") + " - " + rs.getString("Nombre"));
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error obtener nombres equipos: " + e.getMessage()); }
        return nombres;
    }

    public static String obtenerCodigoEquipo() { return generarCodigoEquipo(); }

    public static int totalEquiposPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM tbl_EQUIPO WHERE Estado=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return 0;
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error contar equipos: " + e.getMessage()); }
        return 0;
    }

    // ═══════════════════════════ MANTENIMIENTO ═══════════════════════════

    public static List<Object[]> listarMantenimientos() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT m.ID_mantenimiento, m.Codigo, ISNULL(e.Nombre,'') AS Equipo, ISNULL(e.Codigo,'') AS CodEquipo, ISNULL(e.Area,'') AS Area, ISNULL(m.Descripcion_problema,'') AS Problema, ISNULL(m.Fecha_reporte,'') AS Fecha, ISNULL(m.Prioridad,'Media') AS Prioridad, ISNULL(m.Persona_reporta,'') AS Reporta, ISNULL(t.nombre_tecnico,'Sin asignar') AS Tecnico, ISNULL(m.Estado,'Pendiente') AS Estado, ISNULL(m.Solucion_aplicada,'') AS Solucion, ISNULL(CAST(m.Fecha_solucion AS VARCHAR),'') AS FecSol, ISNULL(m.Tiempo_reparacion,'') AS Tiempo, ISNULL(m.Costo_piezas,0) AS CostoPiezas, ISNULL(m.Costo_mano_obra,0) AS CostoMO, ISNULL(m.Costo_total,0) AS Costo FROM tbl_MANTENIMIENTO m LEFT JOIN tbl_EQUIPO e ON m.fk_ID_equipo=e.ID_equipo LEFT JOIN tbl_TECNICO t ON m.fk_ID_tecnico=t.ID_tecnico ORDER BY m.Fecha_reporte DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) filas.add(new Object[]{rs.getInt("ID_mantenimiento"), rs.getString("Codigo"), rs.getString("Equipo"), rs.getString("CodEquipo"), rs.getString("Area"), rs.getString("Problema"), rs.getString("Fecha"), rs.getString("Prioridad"), rs.getString("Reporta"), rs.getString("Tecnico"), rs.getString("Estado"), rs.getString("Solucion"), rs.getString("FecSol"), rs.getString("Tiempo"), rs.getDouble("CostoPiezas"), rs.getDouble("CostoMO"), rs.getDouble("Costo")});
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error listar mantenimientos: " + e.getMessage()); }
        return filas;
    }

    public static int guardarMantenimiento(String codigo, int idEquipo, String problema, String fecha, String prioridad, String personaReporta) {
        String sql = "INSERT INTO tbl_MANTENIMIENTO (Codigo, fk_ID_equipo, Descripcion_problema, Fecha_reporte, Prioridad, Persona_reporta) OUTPUT INSERTED.ID_mantenimiento VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo); ps.setInt(2, idEquipo); ps.setString(3, problema); ps.setString(4, fecha); ps.setString(5, prioridad); ps.setString(6, personaReporta);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error guardar mantenimiento: " + e.getMessage()); }
        return -1;
    }

    public static boolean asignarTecnico(int id, int idTecnico) {
        String sql = "UPDATE tbl_MANTENIMIENTO SET fk_ID_tecnico=?, Estado='En proceso' WHERE ID_mantenimiento=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, idTecnico); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error asignar tecnico: " + e.getMessage()); }
        return false;
    }

    public static boolean cambiarEstadoMantenimiento(int id, String estado) {
        String sql = "UPDATE tbl_MANTENIMIENTO SET Estado=? WHERE ID_mantenimiento=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, estado); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error cambiar estado: " + e.getMessage()); }
        return false;
    }

    public static boolean finalizarMantenimiento(int id, String solucion, String fechaSol, String tiempo, double costoPiezas, double costoManoObra, String observaciones) {
        String sql = "UPDATE tbl_MANTENIMIENTO SET Estado='Resuelto', Solucion_aplicada=?, Fecha_solucion=?, Tiempo_reparacion=?, Costo_piezas=?, Costo_mano_obra=?, Costo_total=?, Observaciones=? WHERE ID_mantenimiento=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, solucion); ps.setString(2, fechaSol); ps.setString(3, tiempo); ps.setDouble(4, costoPiezas); ps.setDouble(5, costoManoObra); ps.setDouble(6, costoPiezas + costoManoObra); ps.setString(7, observaciones); ps.setInt(8, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error finalizar: " + e.getMessage()); }
        return false;
    }

    public static boolean eliminarMantenimiento(int id) {
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_MANTENIMIENTO WHERE ID_mantenimiento=?")) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error eliminar mtto: " + e.getMessage()); }
        return false;
    }

    public static String obtenerCodigoMantenimiento() { return generarCodigo(); }

    public static int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM tbl_MANTENIMIENTO WHERE Estado=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return 0;
            ps.setString(1, estado);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error contar: " + e.getMessage()); }
        return 0;
    }

    public static double totalCostosMes() {
        String sql = "SELECT ISNULL(SUM(Costo_total),0) FROM tbl_MANTENIMIENTO WHERE MONTH(Fecha_solucion)=MONTH(GETDATE()) AND YEAR(Fecha_solucion)=YEAR(GETDATE()) AND Estado='Resuelto'";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return 0;
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error costos mes: " + e.getMessage()); }
        return 0;
    }

    // ═══════════════════════════ TÉCNICOS ═══════════════════════════

    public static List<Object[]> listarTecnicos() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT ID_tecnico, ISNULL(nombre_tecnico,'') AS nombre, ISNULL(telefono,'') AS telefono, ISNULL(especialidad,'') AS especialidad, ISNULL(maquina_asignada,'') AS maquina, ISNULL(empresa_proviene,'') AS empresa FROM tbl_TECNICO ORDER BY nombre_tecnico";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                String emp = rs.getString("empresa");
                String tipo = (emp != null && !emp.isEmpty()) ? "Externo" : "Interno";
                filas.add(new Object[]{rs.getInt("ID_tecnico"), rs.getString("nombre"), rs.getString("telefono"), rs.getString("especialidad"), tipo, emp, "Activo"});
            }
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error listar tecnicos: " + e.getMessage()); }
        return filas;
    }

    public static int guardarTecnico(String nombre, String telefono, String especialidad, String tipo, String empresa, String estado, String observaciones) {
        String sql = "INSERT INTO tbl_TECNICO (nombre_tecnico, especialidad, telefono, empresa_proviene) OUTPUT INSERTED.ID_tecnico VALUES (?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, nombre); ps.setString(2, especialidad); ps.setString(3, telefono);
            ps.setString(4, "Externo".equals(tipo) ? empresa : "");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error guardar tecnico: " + e.getMessage()); }
        return -1;
    }

    public static boolean actualizarTecnico(int id, String nombre, String telefono, String especialidad, String tipo, String empresa, String estado, String observaciones) {
        String sql = "UPDATE tbl_TECNICO SET nombre_tecnico=?, especialidad=?, telefono=?, empresa_proviene=? WHERE ID_tecnico=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre); ps.setString(2, especialidad); ps.setString(3, telefono);
            ps.setString(4, "Externo".equals(tipo) ? empresa : ""); ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error actualizar tecnico: " + e.getMessage()); }
        return false;
    }

    public static boolean eliminarTecnico(int id) {
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_TECNICO WHERE ID_tecnico=?")) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error eliminar tecnico: " + e.getMessage()); }
        return false;
    }

    public static List<String> obtenerNombresTecnicos() {
        List<String> nombres = new ArrayList<>();
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("SELECT ID_tecnico, nombre_tecnico FROM tbl_TECNICO ORDER BY nombre_tecnico"); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return nombres;
            while (rs.next()) nombres.add(rs.getInt("ID_tecnico") + " - " + rs.getString("nombre_tecnico"));
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error nombres tecnicos: " + e.getMessage()); }
        return nombres;
    }

    // ═══════════════════════════ CALENDARIO ═══════════════════════════

    public static List<Object[]> listarCalendario() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT c.ID_calendario, ISNULL(e.Nombre,'') AS Equipo, ISNULL(CAST(c.Fecha_programada AS VARCHAR),'') AS Fecha, ISNULL(c.Tipo_mantenimiento,'') AS Tipo_mantenimiento, ISNULL(t.nombre_tecnico,'Sin asignar') AS Responsable, ISNULL(c.Estado,'Pendiente') AS Estado, ISNULL(c.Observaciones,'') AS Observaciones FROM tbl_CALENDARIO_MANTENIMIENTO c LEFT JOIN tbl_EQUIPO e ON c.fk_ID_equipo=e.ID_equipo LEFT JOIN tbl_TECNICO t ON c.fk_ID_tecnico=t.ID_tecnico ORDER BY c.Fecha_programada";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) filas.add(new Object[]{rs.getInt("ID_calendario"), rs.getString("Equipo"), rs.getString("Fecha"), rs.getString("Tipo_mantenimiento"), rs.getString("Responsable"), rs.getString("Estado"), rs.getString("Observaciones")});
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error listar calendario: " + e.getMessage()); }
        return filas;
    }

    public static int guardarCalendario(int idEquipo, String fecha, String tipo, int idTecnico, String observaciones) {
        String sql = "INSERT INTO tbl_CALENDARIO_MANTENIMIENTO (fk_ID_equipo, Fecha_programada, Tipo_mantenimiento, fk_ID_tecnico, Observaciones) OUTPUT INSERTED.ID_calendario VALUES (?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, idEquipo); ps.setString(2, fecha); ps.setString(3, tipo); ps.setInt(4, idTecnico); ps.setString(5, observaciones);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error guardar calendario: " + e.getMessage()); }
        return -1;
    }

    public static boolean actualizarCalendario(int id, int idEquipo, String fecha, String tipo, int idTecnico, String estado, String observaciones) {
        String sql = "UPDATE tbl_CALENDARIO_MANTENIMIENTO SET fk_ID_equipo=?, Fecha_programada=?, Tipo_mantenimiento=?, fk_ID_tecnico=?, Estado=?, Observaciones=? WHERE ID_calendario=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, idEquipo); ps.setString(2, fecha); ps.setString(3, tipo); ps.setInt(4, idTecnico); ps.setString(5, estado); ps.setString(6, observaciones); ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error actualizar calendario: " + e.getMessage()); }
        return false;
    }

    public static boolean eliminarCalendario(int id) {
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tbl_CALENDARIO_MANTENIMIENTO WHERE ID_calendario=?")) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error eliminar calendario: " + e.getMessage()); }
        return false;
    }

    public static boolean marcarCalendarioRealizado(int id) {
        String sql = "UPDATE tbl_CALENDARIO_MANTENIMIENTO SET Estado='Realizado' WHERE ID_calendario=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("[MantenimientoDAO] Error marcar calendario: " + e.getMessage()); }
        return false;
    }

    // ═══════════════════════════ DASHBOARD ═══════════════════════════

    public static int equiposBuenEstado() { return totalEquiposPorEstado("Bueno"); }
    public static int equiposConFallas() {
        return totalEquiposPorEstado("Averiado") + totalEquiposPorEstado("En reparación");
    }
    public static int mantenimientosPendientes() { return contarPorEstado("Pendiente"); }
    public static int mantenimientosEnProceso() { return contarPorEstado("En proceso"); }
    public static int mantenimientosFinalizados() { return contarPorEstado("Resuelto"); }
}
