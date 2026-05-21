package org.example.salsiaopf.model;

public class Direccion {
    private int id;
    private int idCliente;
    private String tipo;
    private String direccion;
    private String referencia;
    private boolean principal;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
}
