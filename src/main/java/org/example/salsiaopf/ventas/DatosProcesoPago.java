package org.example.salsiaopf.ventas;

/**
 * Datos capturados en el modal de pago.
 */
public class DatosProcesoPago {

    private final String emailCliente;
    private final MetodoPago metodoPago;
    private final double montoRecibido;
    private final double devuelta;
    private final double total;

    public DatosProcesoPago(String emailCliente, MetodoPago metodoPago,
                             double montoRecibido, double devuelta, double total) {
        this.emailCliente = emailCliente;
        this.metodoPago = metodoPago;
        this.montoRecibido = montoRecibido;
        this.devuelta = devuelta;
        this.total = total;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public double getMontoRecibido() {
        return montoRecibido;
    }

    public double getDevuelta() {
        return devuelta;
    }

    public double getTotal() {
        return total;
    }
}
