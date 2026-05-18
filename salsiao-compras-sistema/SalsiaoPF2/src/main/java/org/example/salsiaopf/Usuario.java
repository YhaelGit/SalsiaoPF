package org.example.salsiaopf;

import java.time.LocalDateTime;

/**
 * Modelo que representa un usuario del sistema.
 * Refleja exactamente la tabla Usuarios de la base de datos.
 */
public class Usuario {

    private int id;
    private String nombre;
    private String usuario;
    private String contrasena;   // hash BCrypt — nunca exponer en texto plano
    private String rol;          // Administrador | Cajero | Cocinero | Repartidor
    private String estado;       // Activo | Inactivo
    private LocalDateTime fechaCreacion;

    // ── Constructor completo ──────────────────────────────────────────────
    public Usuario(int id, String nombre, String usuario,
                   String contrasena, String rol,
                   String estado, LocalDateTime fechaCreacion) {
        this.id             = id;
        this.nombre         = nombre;
        this.usuario        = usuario;
        this.contrasena     = contrasena;
        this.rol            = rol;
        this.estado         = estado;
        this.fechaCreacion  = fechaCreacion;
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public int    getId()            { return id;            }
    public String getNombre()        { return nombre;        }
    public String getUsuario()       { return usuario;       }
    public String getContrasena()    { return contrasena;    }
    public String getRol()           { return rol;           }
    public String getEstado()        { return estado;        }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    // ── Helpers de rol ────────────────────────────────────────────────────
    public boolean esAdministrador() { return "Administrador".equals(rol); }
    public boolean esCajero()        { return "Cajero".equals(rol);        }
    public boolean esCocinero()      { return "Cocinero".equals(rol);      }
    public boolean esRepartidor()    { return "Repartidor".equals(rol);    }

    /** Devuelve un emoji representativo del rol para mostrar en la UI. */
    public String getIconoRol() {
        return switch (rol) {
            case "Administrador" -> "👑";
            case "Cajero"        -> "💰";
            case "Cocinero"      -> "👨‍🍳";
            case "Repartidor"    -> "🛵";
            default              -> "👤";
        };
    }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}

