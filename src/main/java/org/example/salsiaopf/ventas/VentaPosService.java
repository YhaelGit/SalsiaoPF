package org.example.salsiaopf.ventas;

import org.example.salsiaopf.service.PagoService;

/**
 * Fachada de compatibilidad: delega al flujo POS central en {@link PagoService}.
 */
public final class VentaPosService {

    private VentaPosService() {
    }

    public static String generarIdFactura() {
        return PagoService.generarIdFactura();
    }

    public static ResultadoProcesoVenta procesar(CarritoVentas carrito, DatosProcesoPago pago) {
        return PagoService.procesarPago(carrito, pago);
    }
}
