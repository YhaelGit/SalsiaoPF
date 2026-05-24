package org.example.salsiaopf.dao;

import org.example.salsiaopf.database.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    public static void crearTablasSiNoExisten() {
        String[] sqls = {
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_EMPLEADO') BEGIN
                CREATE TABLE tbl_EMPLEADO (
                    ID_empleado INT IDENTITY(1,1) PRIMARY KEY,
                    Codigo VARCHAR(20) NOT NULL UNIQUE,
                    Nombre VARCHAR(100) NOT NULL,
                    Apellido VARCHAR(100) NOT NULL,
                    Cedula VARCHAR(20) NOT NULL UNIQUE,
                    Telefono VARCHAR(20) NULL,
                    Direccion VARCHAR(255) NULL,
                    Correo VARCHAR(100) NULL,
                    Cargo VARCHAR(50) NULL,
                    Area VARCHAR(50) NULL,
                    Turno VARCHAR(20) NULL,
                    Sueldo DECIMAL(10,2) NOT NULL DEFAULT 0,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                    Fecha_ingreso DATE NULL,
                    Observacion VARCHAR(500) NULL,
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CARGO') BEGIN
                CREATE TABLE tbl_CARGO (
                    ID_cargo INT IDENTITY(1,1) PRIMARY KEY,
                    Nombre VARCHAR(100) NOT NULL UNIQUE,
                    Descripcion VARCHAR(255) NULL,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_ASISTENCIA') BEGIN
                CREATE TABLE tbl_ASISTENCIA (
                    ID_asistencia INT IDENTITY(1,1) PRIMARY KEY,
                    fk_ID_empleado INT NOT NULL,
                    Fecha DATE NOT NULL,
                    Hora_entrada VARCHAR(10) NULL,
                    Hora_salida VARCHAR(10) NULL,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Presente',
                    Observacion VARCHAR(255) NULL,
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_TURNO') BEGIN
                CREATE TABLE tbl_TURNO (
                    ID_turno INT IDENTITY(1,1) PRIMARY KEY,
                    fk_ID_empleado INT NOT NULL,
                    Dia VARCHAR(20) NOT NULL,
                    Hora_entrada VARCHAR(10) NOT NULL,
                    Hora_salida VARCHAR(10) NOT NULL,
                    Observacion VARCHAR(255) NULL,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_USUARIO_SISTEMA') BEGIN
                CREATE TABLE tbl_USUARIO_SISTEMA (
                    ID_usuario INT IDENTITY(1,1) PRIMARY KEY,
                    fk_ID_empleado INT NOT NULL,
                    Usuario VARCHAR(50) NOT NULL UNIQUE,
                    Contrasena VARCHAR(100) NOT NULL,
                    Rol VARCHAR(50) NOT NULL,
                    Estado VARCHAR(20) NOT NULL DEFAULT 'Activo',
                    Permisos VARCHAR(500) NULL,
                    Ultimo_acceso DATETIME NULL,
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_HISTORIAL_EMPLEADO') BEGIN
                CREATE TABLE tbl_HISTORIAL_EMPLEADO (
                    ID_historial INT IDENTITY(1,1) PRIMARY KEY,
                    fk_ID_empleado INT NOT NULL,
                    Tipo_evento VARCHAR(50) NOT NULL,
                    Descripcion VARCHAR(500) NULL,
                    Usuario VARCHAR(100) NULL,
                    Modulo VARCHAR(50) NULL,
                    Fecha DATE NOT NULL,
                    Hora VARCHAR(10) NULL,
                    fecha_creacion DATETIME DEFAULT GETDATE()
                );
            END
            """,
            """
            IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='tbl_CONTADOR_EMPLEADO') BEGIN
                CREATE TABLE tbl_CONTADOR_EMPLEADO (
                    fecha DATE PRIMARY KEY,
                    ultimo_numero INT NOT NULL DEFAULT 0
                );
            END
            """
        };
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (Statement stmt = conn.createStatement()) {
                for (String sql : sqls) stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error creando tablas: " + e.getMessage());
        }
    }

    public static synchronized String generarCodigoEmpleado() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return "EMP-0001";
            LocalDate hoy = LocalDate.now();
            int numero;
            try (PreparedStatement ps = conn.prepareStatement("SELECT ultimo_numero FROM tbl_CONTADOR_EMPLEADO WHERE fecha = ?")) {
                ps.setDate(1, Date.valueOf(hoy));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    numero = rs.getInt(1) + 1;
                    try (PreparedStatement pu = conn.prepareStatement("UPDATE tbl_CONTADOR_EMPLEADO SET ultimo_numero = ultimo_numero + 1 WHERE fecha = ?")) {
                        pu.setDate(1, Date.valueOf(hoy));
                        pu.executeUpdate();
                    }
                } else {
                    numero = 1;
                    try (PreparedStatement pi = conn.prepareStatement("INSERT INTO tbl_CONTADOR_EMPLEADO (fecha, ultimo_numero) VALUES (?, 1)")) {
                        pi.setDate(1, Date.valueOf(hoy));
                        pi.executeUpdate();
                    }
                }
            }
            return String.format("EMP-%04d", numero);
        } catch (SQLException e) {
            return "EMP-" + System.currentTimeMillis() % 10000;
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  EMPLEADOS CRUD
    // ═══════════════════════════════════════════════════════════

    public static int guardarEmpleado(String nombre, String apellido, String cedula, String telefono,
                                       String direccion, String correo, String cargo, String area,
                                       String turno, LocalDate fechaIngreso, double sueldo,
                                       String estado, String observacion) {
        crearTablasSiNoExisten();
        String codigo = generarCodigoEmpleado();
        String sql = "INSERT INTO tbl_EMPLEADO (Codigo, Nombre, Apellido, Cedula, Telefono, direccion, Cargo, Area, Turno, Sueldo, estatus, Fecha_ingreso, fk_ID_departamento, fk_ID_cargo) OUTPUT INSERTED.ID_empleado VALUES (?,?,?,?,?,?,?,?,?,?,?,?,1,1)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.setString(4, cedula);
            ps.setString(5, telefono.isEmpty() ? null : telefono);
            ps.setString(6, direccion.isEmpty() ? null : direccion);
            ps.setString(7, cargo.isEmpty() ? null : cargo);
            ps.setString(8, area.isEmpty() ? null : area);
            ps.setString(9, turno.isEmpty() ? null : turno);
            ps.setDouble(10, sueldo);
            ps.setString(11, estado);
            ps.setDate(12, fechaIngreso != null ? Date.valueOf(fechaIngreso) : null);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando: " + e.getMessage());
        }
        return -1;
    }

    public static boolean actualizarEmpleado(int id, String nombre, String apellido, String cedula, String telefono,
                                              String direccion, String correo, String cargo, String area,
                                              String turno, LocalDate fechaIngreso, double sueldo,
                                              String estado, String observacion) {
        String sql = "UPDATE tbl_EMPLEADO SET Nombre=?, Apellido=?, Cedula=?, Telefono=?, direccion=?, Cargo=?, Area=?, Turno=?, Sueldo=?, estatus=?, Fecha_ingreso=?, fk_ID_departamento=1, fk_ID_cargo=1 WHERE ID_empleado=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, cedula);
            ps.setString(4, telefono.isEmpty() ? null : telefono);
            ps.setString(5, direccion.isEmpty() ? null : direccion);
            ps.setString(6, cargo.isEmpty() ? null : cargo);
            ps.setString(7, area.isEmpty() ? null : area);
            ps.setString(8, turno.isEmpty() ? null : turno);
            ps.setDouble(9, sueldo);
            ps.setString(10, estado);
            ps.setDate(11, fechaIngreso != null ? Date.valueOf(fechaIngreso) : null);
            ps.setInt(12, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error actualizando: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarEmpleado(int id) {
        String sql = "DELETE FROM tbl_EMPLEADO WHERE ID_empleado=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error eliminando: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarEmpleados() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT ID_empleado, Codigo, Nombre, Apellido, Cedula, Telefono, ISNULL(direccion,'') AS Direccion, ISNULL(Cargo,'') AS Cargo, ISNULL(Area,'') AS Area, ISNULL(Turno,'') AS Turno, Sueldo, estatus, Fecha_ingreso FROM tbl_EMPLEADO ORDER BY ID_empleado DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_empleado"), rs.getString("Codigo"),
                    rs.getString("Nombre"), rs.getString("Apellido"),
                    rs.getString("Cedula"), rs.getString("Telefono") != null ? rs.getString("Telefono") : "",
                    rs.getString("Direccion") != null ? rs.getString("Direccion") : "",
                    "", rs.getString("Cargo"),
                    rs.getString("Area"), rs.getString("Turno"),
                    rs.getDouble("Sueldo"), rs.getString("estatus"),
                    rs.getDate("Fecha_ingreso") != null ? rs.getDate("Fecha_ingreso").toLocalDate() : null,
                    ""
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando: " + e.getMessage());
        }
        return filas;
    }

    public static Object[] obtenerEmpleado(int id) {
        String sql = "SELECT ID_empleado, Codigo, Nombre, Apellido, Cedula, Telefono, ISNULL(direccion,'') AS Direccion, ISNULL(Cargo,'') AS Cargo, ISNULL(Area,'') AS Area, ISNULL(Turno,'') AS Turno, Sueldo, estatus, Fecha_ingreso FROM tbl_EMPLEADO WHERE ID_empleado=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return null;
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                        rs.getInt("ID_empleado"), rs.getString("Codigo"),
                        rs.getString("Nombre"), rs.getString("Apellido"),
                        rs.getString("Cedula"), rs.getString("Telefono") != null ? rs.getString("Telefono") : "",
                        rs.getString("Direccion") != null ? rs.getString("Direccion") : "",
                        "", rs.getString("Cargo") != null ? rs.getString("Cargo") : "",
                        rs.getString("Area") != null ? rs.getString("Area") : "",
                        rs.getString("Turno") != null ? rs.getString("Turno") : "",
                        rs.getDouble("Sueldo"), rs.getString("estatus"),
                        rs.getDate("Fecha_ingreso") != null ? rs.getDate("Fecha_ingreso").toLocalDate() : null,
                        ""
                    };
                }
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error obteniendo: " + e.getMessage());
        }
        return null;
    }

    public static int contarActivos() {
        String sql = "SELECT COUNT(*) FROM tbl_EMPLEADO WHERE estatus='Activo'";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return 0;
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[EmpleadoDAO] Error conteo: " + e.getMessage()); }
        return 0;
    }

    public static int contarInactivos() {
        String sql = "SELECT COUNT(*) FROM tbl_EMPLEADO WHERE estatus!='Activo'";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return 0;
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[EmpleadoDAO] Error conteo: " + e.getMessage()); }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    //  CARGOS / ROLES
    // ═══════════════════════════════════════════════════════════

    public static int guardarCargo(String nombre, String descripcion) {
        String sql = "INSERT INTO tbl_CARGO (Nombre, Descripcion) OUTPUT INSERTED.ID_cargo VALUES (?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setString(1, nombre);
            ps.setString(2, descripcion.isEmpty() ? null : descripcion);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando cargo: " + e.getMessage());
        }
        return -1;
    }

    public static boolean actualizarCargo(int id, String nombre, String descripcion) {
        String sql = "UPDATE tbl_CARGO SET Nombre=?, Descripcion=? WHERE ID_cargo=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nombre);
            ps.setString(2, descripcion.isEmpty() ? null : descripcion);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error actualizando cargo: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarCargo(int id) {
        String sql = "DELETE FROM tbl_CARGO WHERE ID_cargo=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error eliminando cargo: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarCargos() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT c.ID_cargo, c.Nombre, ISNULL(c.Descripcion,'') AS Descripcion FROM tbl_CARGO c ORDER BY c.Nombre";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_cargo"), rs.getString("Nombre"),
                    rs.getString("Descripcion")
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando cargos: " + e.getMessage());
        }
        return filas;
    }

    public static List<String> obtenerNombresCargos() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT Nombre FROM tbl_CARGO ORDER BY Nombre";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return nombres;
            while (rs.next()) nombres.add(rs.getString("Nombre"));
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error obteniendo nombres cargos: " + e.getMessage());
        }
        return nombres;
    }

    // ═══════════════════════════════════════════════════════════
    //  ASISTENCIA
    // ═══════════════════════════════════════════════════════════

    public static int guardarAsistencia(int fkEmpleado, LocalDate fecha, String entrada, String salida, String estado, String obs) {
        String sql = "INSERT INTO tbl_ASISTENCIA (fk_ID_empleado, Fecha, Hora_entrada, Hora_salida, Estado, Observacion) OUTPUT INSERTED.ID_asistencia VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, fkEmpleado);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setString(3, entrada.isEmpty() ? null : entrada);
            ps.setString(4, salida.isEmpty() ? null : salida);
            ps.setString(5, estado);
            ps.setString(6, obs.isEmpty() ? null : obs);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando asistencia: " + e.getMessage());
        }
        return -1;
    }

    public static List<Object[]> listarAsistencias() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT a.ID_asistencia, e.Codigo, e.Nombre, e.Apellido, a.Fecha, ISNULL(a.Hora_entrada,'') AS Entrada, ISNULL(a.Hora_salida,'') AS Salida, a.Estado, ISNULL(a.Observacion,'') AS Obs FROM tbl_ASISTENCIA a LEFT JOIN tbl_EMPLEADO e ON a.fk_ID_empleado=e.ID_empleado ORDER BY a.Fecha DESC, a.ID_asistencia DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_asistencia"),
                    rs.getString("Nombre") + " " + rs.getString("Apellido"),
                    rs.getDate("Fecha").toLocalDate(), rs.getString("Entrada"),
                    rs.getString("Salida"), rs.getString("Estado"), rs.getString("Obs")
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando asistencias: " + e.getMessage());
        }
        return filas;
    }

    public static int contarAsistenciasHoy() {
        String sql = "SELECT COUNT(*) FROM tbl_ASISTENCIA WHERE Fecha=CAST(GETDATE() AS DATE) AND Estado='Presente'";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return 0;
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.out.println("[EmpleadoDAO] Error conteo: " + e.getMessage()); }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    //  TURNOS
    // ═══════════════════════════════════════════════════════════

    public static int guardarTurno(int fkEmpleado, String dia, String entrada, String salida, String obs) {
        String sql = "INSERT INTO tbl_TURNO (fk_ID_empleado, Dia, Hora_entrada, Hora_salida, Observacion) OUTPUT INSERTED.ID_turno VALUES (?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, fkEmpleado);
            ps.setString(2, dia);
            ps.setString(3, entrada);
            ps.setString(4, salida);
            ps.setString(5, obs.isEmpty() ? null : obs);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando turno: " + e.getMessage());
        }
        return -1;
    }

    public static boolean actualizarTurno(int id, String dia, String entrada, String salida, String obs) {
        String sql = "UPDATE tbl_TURNO SET Dia=?, Hora_entrada=?, Hora_salida=?, Observacion=? WHERE ID_turno=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, dia);
            ps.setString(2, entrada);
            ps.setString(3, salida);
            ps.setString(4, obs.isEmpty() ? null : obs);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error actualizando turno: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarTurno(int id) {
        String sql = "DELETE FROM tbl_TURNO WHERE ID_turno=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error eliminando turno: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarTurnos() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT t.ID_turno, e.Codigo, e.Nombre, e.Apellido, e.Cargo, t.Dia, t.Hora_entrada, t.Hora_salida, ISNULL(t.Observacion,'') AS Obs, t.Estado FROM tbl_TURNO t LEFT JOIN tbl_EMPLEADO e ON t.fk_ID_empleado=e.ID_empleado ORDER BY t.ID_turno DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_turno"),
                    rs.getString("Nombre") + " " + rs.getString("Apellido"),
                    rs.getString("Cargo") != null ? rs.getString("Cargo") : "",
                    rs.getString("Dia"), rs.getString("Hora_entrada"),
                    rs.getString("Hora_salida"), rs.getString("Estado")
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando turnos: " + e.getMessage());
        }
        return filas;
    }

    // ═══════════════════════════════════════════════════════════
    //  USUARIOS DEL SISTEMA
    // ═══════════════════════════════════════════════════════════

    public static int guardarUsuario(int fkEmpleado, String usuario, String contrasena, String rol, String estado, String permisos) {
        String sql = "INSERT INTO tbl_USUARIO_SISTEMA (fk_ID_empleado, Usuario, Contrasena, Rol, Estado, Permisos) OUTPUT INSERTED.ID_usuario VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, fkEmpleado);
            ps.setString(2, usuario);
            ps.setString(3, contrasena);
            ps.setString(4, rol);
            ps.setString(5, estado);
            ps.setString(6, permisos.isEmpty() ? null : permisos);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando usuario: " + e.getMessage());
        }
        return -1;
    }

    public static boolean actualizarUsuario(int id, String usuario, String rol, String estado, String permisos) {
        String sql = "UPDATE tbl_USUARIO_SISTEMA SET Usuario=?, Rol=?, Estado=?, Permisos=? WHERE ID_usuario=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, usuario);
            ps.setString(2, rol);
            ps.setString(3, estado);
            ps.setString(4, permisos.isEmpty() ? null : permisos);
            ps.setInt(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error actualizando usuario: " + e.getMessage());
            return false;
        }
    }

    public static boolean cambiarContrasena(int id, String nuevaContrasena) {
        String sql = "UPDATE tbl_USUARIO_SISTEMA SET Contrasena=? WHERE ID_usuario=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setString(1, nuevaContrasena);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error cambiando contraseña: " + e.getMessage());
            return false;
        }
    }

    public static boolean eliminarUsuario(int id) {
        String sql = "DELETE FROM tbl_USUARIO_SISTEMA WHERE ID_usuario=?";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return false;
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error eliminando usuario: " + e.getMessage());
            return false;
        }
    }

    public static List<Object[]> listarUsuarios() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT u.ID_usuario, e.Codigo, e.Nombre, e.Apellido, u.Usuario, u.Rol, u.Estado, ISNULL(u.Permisos,'') AS Permisos, u.Ultimo_acceso FROM tbl_USUARIO_SISTEMA u LEFT JOIN tbl_EMPLEADO e ON u.fk_ID_empleado=e.ID_empleado ORDER BY u.ID_usuario DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getInt("ID_usuario"),
                    rs.getString("Nombre") + " " + rs.getString("Apellido"),
                    rs.getString("Usuario"), rs.getString("Rol"),
                    rs.getString("Estado"),
                    rs.getTimestamp("Ultimo_acceso") != null ? rs.getTimestamp("Ultimo_acceso").toLocalDateTime().toString() : "Nunca"
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando usuarios: " + e.getMessage());
        }
        return filas;
    }

    // ═══════════════════════════════════════════════════════════
    //  HISTORIAL
    // ═══════════════════════════════════════════════════════════

    public static int guardarHistorial(int fkEmpleado, String tipoEvento, String descripcion, String usuario, String modulo) {
        String sql = "INSERT INTO tbl_HISTORIAL_EMPLEADO (fk_ID_empleado, Tipo_evento, Descripcion, Usuario, Modulo, Fecha, Hora) OUTPUT INSERTED.ID_historial VALUES (?,?,?,?,?,CAST(GETDATE() AS DATE),FORMAT(GETDATE(),'hh:mm tt'))";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (conn == null) return -1;
            ps.setInt(1, fkEmpleado);
            ps.setString(2, tipoEvento);
            ps.setString(3, descripcion.isEmpty() ? null : descripcion);
            ps.setString(4, usuario.isEmpty() ? null : usuario);
            ps.setString(5, modulo.isEmpty() ? null : modulo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error guardando historial: " + e.getMessage());
        }
        return -1;
    }

    public static List<Object[]> listarHistorial() {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT h.ID_historial, h.Fecha, h.Hora, e.Codigo, e.Nombre, e.Apellido, h.Tipo_evento, ISNULL(h.Descripcion,'') AS Descripcion, ISNULL(h.Usuario,'') AS Usuario, ISNULL(h.Modulo,'') AS Modulo FROM tbl_HISTORIAL_EMPLEADO h LEFT JOIN tbl_EMPLEADO e ON h.fk_ID_empleado=e.ID_empleado ORDER BY h.Fecha DESC, h.ID_historial DESC";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return filas;
            while (rs.next()) {
                filas.add(new Object[]{
                    rs.getDate("Fecha") != null ? rs.getDate("Fecha").toLocalDate() : null,
                    rs.getString("Hora") != null ? rs.getString("Hora") : "",
                    rs.getString("Nombre") + " " + rs.getString("Apellido"),
                    rs.getString("Tipo_evento"), rs.getString("Descripcion"),
                    rs.getString("Usuario"), rs.getString("Modulo")
                });
            }
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error listando historial: " + e.getMessage());
        }
        return filas;
    }

    public static List<String> obtenerNombresEmpleadosActivos() {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT ID_empleado, Nombre, Apellido FROM tbl_EMPLEADO WHERE estatus='Activo' ORDER BY Nombre";
        try (Connection conn = ConexionBD.conectar(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (conn == null) return nombres;
            while (rs.next()) nombres.add(rs.getInt("ID_empleado") + "|" + rs.getString("Nombre") + " " + rs.getString("Apellido"));
        } catch (SQLException e) {
            System.out.println("[EmpleadoDAO] Error obteniendo nombres: " + e.getMessage());
        }
        return nombres;
    }
}
