package org.example.salsiaopf.util;

import org.example.salsiaopf.model.Usuario;

/**
 * Singleton que mantiene la sesión del usuario activo durante toda la ejecución.
 *
 * Uso:
 *   SessionManager.getInstance().iniciarSesion(usuario);
 *   SessionManager.getInstance().getUsuarioActivo();
 *   SessionManager.getInstance().cerrarSesion();
 */
public class SessionManager {

    // ── Singleton ────────────────────────────────────────────────────────
    private static SessionManager instancia;

    private SessionManager() { }

    public static SessionManager getInstance() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    // ── Estado de sesión ─────────────────────────────────────────────────
    private Usuario usuarioActivo;

    /**
     * Registra el usuario autenticado como sesión activa.
     * @param usuario Usuario validado con datos de la BD
     */
    public void iniciarSesion(Usuario usuario) {
        this.usuarioActivo = usuario;
        System.out.println("[SessionManager] Sesión iniciada: " + usuario.getNombre()
                + " | Rol: " + usuario.getRol());
    }

    /**
     * Cierra la sesión activa (pone usuarioActivo en null).
     */
    public void cerrarSesion() {
        if (usuarioActivo != null) {
            System.out.println("[SessionManager] Sesión cerrada para: " + usuarioActivo.getNombre());
        }
        usuarioActivo = null;
    }

    /** @return El usuario actualmente autenticado, o null si no hay sesión. */
    public Usuario getUsuarioActivo() {
        return usuarioActivo;
    }

    /** @return true si hay un usuario autenticado en este momento. */
    public boolean haySesionActiva() {
        return usuarioActivo != null;
    }

    // ── Helpers de rol (para evitar repetir lógica en controllers) ───────
    public boolean esAdministrador() {
        return haySesionActiva() && usuarioActivo.esAdministrador();
    }

    public boolean esCajero() {
        return haySesionActiva() && usuarioActivo.esCajero();
    }

    public boolean esInventario() {
        return haySesionActiva() && usuarioActivo.esInventario();
    }

    public boolean esDelivery() {
        return haySesionActiva() && usuarioActivo.esDelivery();
    }

    /** @return El rol del usuario activo, o cadena vacía si no hay sesión. */
    public String getRolActivo() {
        return haySesionActiva() ? usuarioActivo.getRol() : "";
    }
}
