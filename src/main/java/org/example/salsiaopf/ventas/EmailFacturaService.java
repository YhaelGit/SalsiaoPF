package org.example.salsiaopf.ventas;

import org.example.salsiaopf.service.EmailService;

import java.nio.file.Path;

/**
 * @deprecated Usar {@link org.example.salsiaopf.service.EmailService}
 */
@Deprecated
public final class EmailFacturaService {

    private EmailFacturaService() {
    }

    public static boolean estaConfigurado() {
        return EmailService.estaConfigurado();
    }

    public static String enviarFactura(String emailDestino, Path pdf, String idFactura, double total) {
        int id = 0;
        try {
            if (idFactura != null && idFactura.contains("-")) {
                String[] partes = idFactura.split("-");
                id = Integer.parseInt(partes[partes.length - 1].replaceAll("\\D", "0"));
            }
        } catch (Exception ignored) {
        }
        return EmailService.enviarFactura(emailDestino, pdf, id, total);
    }
}
