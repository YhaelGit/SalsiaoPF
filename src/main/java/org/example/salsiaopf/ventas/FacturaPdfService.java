package org.example.salsiaopf.ventas;

import org.example.salsiaopf.service.FacturaService;

import java.nio.file.Path;

/**
 * @deprecated Reemplazado por {@link org.example.salsiaopf.service.FacturaService} (JasperReports).
 */
@Deprecated
public final class FacturaPdfService {

    private FacturaPdfService() {
    }

    public static Path generar(String idFactura, int idVenta, java.util.List<ItemCarrito> items,
                               DatosProcesoPago pago) throws Exception {
        return FacturaService.generarFacturaPdf(idVenta);
    }
}
