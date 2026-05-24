package org.example.salsiaopf.model;

/**
 * @deprecated Sin uso actual en el sistema. Clase mantenida por compatibilidad futura.
 */
@Deprecated
public class Proveedor {
    private int id;
    private String rnc;
    private String nombre;
    private String telefono;
    private String direccion;
    private String correo;
    private String estado;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRnc() { return rnc; }
    public void setRnc(String rnc) { this.rnc = rnc; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
