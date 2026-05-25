package org.example.salsiaopf.model;

import java.time.LocalDateTime;

public class Caja {
    private int idCaja;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private double montoInicial;
    private double totalEfectivo;
    private double totalTarjeta;
    private double totalTransferencia;
    private double totalGeneral;
    private double diferencia;
    private String usuario;
    private String estado;

    public int getIdCaja() { return idCaja; }
    public void setIdCaja(int idCaja) { this.idCaja = idCaja; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime fechaApertura) { this.fechaApertura = fechaApertura; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(double montoInicial) { this.montoInicial = montoInicial; }
    public double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(double totalEfectivo) { this.totalEfectivo = totalEfectivo; }
    public double getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(double totalTarjeta) { this.totalTarjeta = totalTarjeta; }
    public double getTotalTransferencia() { return totalTransferencia; }
    public void setTotalTransferencia(double totalTransferencia) { this.totalTransferencia = totalTransferencia; }
    public double getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(double totalGeneral) { this.totalGeneral = totalGeneral; }
    public double getDiferencia() { return diferencia; }
    public void setDiferencia(double diferencia) { this.diferencia = diferencia; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
