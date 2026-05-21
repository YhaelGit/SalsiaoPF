package org.example.salsiaopf.ventas;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Abre e imprime facturas PDF con java.awt.Desktop.
 */
public final class FacturaDesktopUtil {

    private FacturaDesktopUtil() {
    }

    public static boolean abrirPdf(Path rutaPdf) {
        if (rutaPdf == null || !Files.exists(rutaPdf)) {
            return false;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(rutaPdf.toFile());
                return true;
            }
        } catch (IOException e) {
            System.out.println("[FacturaDesktop] No se pudo abrir PDF: " + e.getMessage());
        }
        return false;
    }

    public static boolean imprimirPdf(Path rutaPdf) {
        if (rutaPdf == null || !Files.exists(rutaPdf)) {
            return false;
        }
        try {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.PRINT)) {
                desktop.print(rutaPdf.toFile());
                return true;
            }
        } catch (IOException e) {
            System.out.println("[FacturaDesktop] No se pudo imprimir PDF: " + e.getMessage());
        }
        return false;
    }
}
