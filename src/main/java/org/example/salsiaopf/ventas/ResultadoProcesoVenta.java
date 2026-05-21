package org.example.salsiaopf.ventas;

import java.nio.file.Path;

/**
 * Resultado del flujo POS (BD + PDF + correo).
 */
public class ResultadoProcesoVenta {

    private final boolean exito;
    private final int idVenta;
    private final String idFactura;
    private final Path rutaPdf;
    private final boolean correoEnviado;
    private final String mensajeCorreo;
    private final String mensajeError;

    public ResultadoProcesoVenta(boolean exito, int idVenta, String idFactura, Path rutaPdf,
                                 boolean correoEnviado, String mensajeCorreo, String mensajeError) {
        this.exito = exito;
        this.idVenta = idVenta;
        this.idFactura = idFactura;
        this.rutaPdf = rutaPdf;
        this.correoEnviado = correoEnviado;
        this.mensajeCorreo = mensajeCorreo;
        this.mensajeError = mensajeError;
    }

    public static ResultadoProcesoVenta error(String mensaje) {
        return new ResultadoProcesoVenta(false, -1, null, null, false, null, mensaje);
    }

    public boolean isExito() {
        return exito;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public String getIdFactura() {
        return idFactura;
    }

    public Path getRutaPdf() {
        return rutaPdf;
    }

    public boolean isCorreoEnviado() {
        return correoEnviado;
    }

    public String getMensajeCorreo() {
        return mensajeCorreo;
    }

    public String getMensajeError() {
        return mensajeError;
    }
}
