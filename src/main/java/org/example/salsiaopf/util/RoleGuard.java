package org.example.salsiaopf.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Set;

/**
 * Guarda de roles: verifica si el usuario activo tiene permiso
 * para acceder a un módulo determinado.
 *
 * Uso básico en cualquier controller o botón:
 *   if (!RoleGuard.permitir("Compras")) return;
 */
public class RoleGuard {

    // ── Permisos por módulo ────────────────────────────────────────────
    // Clave: nombre del módulo (en minúscula).
    // Valor: roles que tienen acceso.

    private static final java.util.Map<String, Set<String>> PERMISOS = java.util.Map.of(
        "compras",       Set.of("Administrador", "Inventario"),
        "empleados",     Set.of("Administrador"),
        "mantenimiento", Set.of("Administrador"),
        "inventario",    Set.of("Administrador", "Inventario"),
        "ventas",        Set.of("Administrador", "Cajero"),
        "clientes",      Set.of("Administrador", "Cajero"),
        "reportes",      Set.of("Administrador")
    );

    /**
     * Verifica si el usuario activo puede acceder al módulo indicado.
     * Si no tiene permiso, muestra automáticamente un diálogo "Acceso Denegado".
     *
     * @param modulo Nombre del módulo (p.ej. "Ventas", "Compras")
     * @return true si se permite el acceso, false en caso contrario
     */
    public static boolean permitir(String modulo) {
        SessionManager session = SessionManager.getInstance();

        // Si no hay sesión activa, rechazar siempre
        if (!session.haySesionActiva()) {
            mostrarAccesoDenegado("No hay sesión activa. Inicie sesión primero.");
            return false;
        }

        String rol       = session.getRolActivo();
        String moduloKey = modulo.toLowerCase();

        // Si el módulo no está en el mapa, solo el administrador puede
        Set<String> rolesPermitidos = PERMISOS.getOrDefault(moduloKey, Set.of("Administrador"));

        if (rolesPermitidos.contains(rol)) {
            return true;   // ✅ Acceso concedido
        }

        // ❌ Acceso denegado
        mostrarAccesoDenegado(
            "Tu rol  «" + rol + "»  no tiene permiso para acceder al módulo «" + modulo + "»."
        );
        return false;
    }

    /**
     * Versión silenciosa: retorna true/false sin mostrar diálogo.
     * Útil para ocultar/mostrar botones dinámicamente.
     */
    public static boolean tienePermiso(String modulo) {
        SessionManager session = SessionManager.getInstance();
        if (!session.haySesionActiva()) return false;

        String rol       = session.getRolActivo();
        String moduloKey = modulo.toLowerCase();
        Set<String> rolesPermitidos = PERMISOS.getOrDefault(moduloKey, Set.of("Administrador"));
        return rolesPermitidos.contains(rol);
    }

    // ── Diálogo de acceso denegado ─────────────────────────────────────
    public static void mostrarAccesoDenegado(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🔒 Acceso Denegado");
        alert.setHeaderText("No tienes permiso para realizar esta acción");
        alert.setContentText(mensaje);
        alert.getDialogPane().setStyle(
            "-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;"
        );
        alert.showAndWait();
    }
}
