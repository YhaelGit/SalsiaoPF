package org.example.salsiaopf.model;

import java.time.LocalDate;

public class Cliente {
    private int idCliente;
    private String codigo;
    private String nombre;
    private String apellido;
    private String cedula;
    private String telefono;
    private String direccion;
    private String email;
    private String tipoCliente;
    private String estado;
    private String observaciones;
    private LocalDate fechaCreacion;
    private LocalDate ultimaCompra;
    private double totalCompras;
    private int cantidadCompras;

    public Cliente() {}

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipoCliente() { return tipoCliente; }
    public void setTipoCliente(String tipoCliente) { this.tipoCliente = tipoCliente; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDate getUltimaCompra() { return ultimaCompra; }
    public void setUltimaCompra(LocalDate ultimaCompra) { this.ultimaCompra = ultimaCompra; }
    public double getTotalCompras() { return totalCompras; }
    public void setTotalCompras(double totalCompras) { this.totalCompras = totalCompras; }
    public int getCantidadCompras() { return cantidadCompras; }
    public void setCantidadCompras(int cantidadCompras) { this.cantidadCompras = cantidadCompras; }

    @Override
    public String toString() { return codigo + " - " + nombre + " " + apellido; }
}
