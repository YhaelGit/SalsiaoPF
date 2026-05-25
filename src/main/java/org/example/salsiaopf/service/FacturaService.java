package org.example.salsiaopf.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Caja;
import org.example.salsiaopf.ventas.ItemCarrito;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class FacturaService {

    private static final String REPORTE_JRXML = "/reports/factura_salsiao.jrxml";
    private static final String REPORTE_DIRECTO_JRXML = "/reports/factura_directa.jrxml";
    private static final String REPORTE_CAJA_JRXML = "/reports/caja_resumen.jrxml";
    private static final DateTimeFormatter ID_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
    private static final double ITBIS_PORCENTAJE = 0.18;

    private FacturaService() {
    }

    public static Path generarFacturaPdf(int idVenta) throws Exception {
        System.out.println("[FacturaService] Generando factura PDF para venta #" + idVenta);

        Path directorio = Path.of(System.getProperty("user.dir"), "facturas");
        Files.createDirectories(directorio);
        Path archivoPdf = directorio.resolve("FACTURA-" + idVenta + ".pdf");

        java.io.InputStream jrxmlStream = FacturaService.class.getResourceAsStream(REPORTE_JRXML);
        if (jrxmlStream == null) {
            throw new IllegalStateException("No se encontró el reporte: " + REPORTE_JRXML);
        }

        JasperReport reporte = JasperCompileManager.compileReport(jrxmlStream);
        jrxmlStream.close();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("ID_VENTA", idVenta);

        Connection conn = ConexionBD.conectar();
        if (conn == null) {
            throw new IllegalStateException("Sin conexión a SQL Server.");
        }

        try {
            JasperPrint impresion = JasperFillManager.fillReport(reporte, parametros, conn);
            JasperExportManager.exportReportToPdfFile(impresion, archivoPdf.toAbsolutePath().toString());
        } finally {
            conn.close();
        }

        if (!Files.exists(archivoPdf) || Files.size(archivoPdf) == 0) {
            throw new IllegalStateException("El PDF generado está vacío.");
        }

        System.out.println("[FacturaService] PDF generado exitosamente: " + archivoPdf.toAbsolutePath());
        return archivoPdf;
    }

    public static Path generarFacturaDirecta(List<ItemCarrito> items, String clienteNombre,
                                              String clienteTelefono, String clienteDireccion,
                                              String clienteEmail, String metodoPago,
                                              BigDecimal descuento, BigDecimal delivery,
                                              BigDecimal montoRecibido, BigDecimal devuelta) throws Exception {
        System.out.println("[FacturaService] Generando factura directa desde carrito...");

        Path directorio = Path.of(System.getProperty("user.dir"), "facturas");
        Files.createDirectories(directorio);

        String invoiceNum = generarNumeroFactura();
        String invoiceDate = LocalDateTime.now().format(DATE_FMT);

        BigDecimal subtotal = calcularSubtotal(items);
        BigDecimal itbis = subtotal.multiply(BigDecimal.valueOf(ITBIS_PORCENTAJE)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(itbis).subtract(descuento).add(delivery);

        Path archivoPdf = directorio.resolve(invoiceNum.replace("/", "-") + ".pdf");

        java.io.InputStream jrxmlStream = FacturaService.class.getResourceAsStream(REPORTE_DIRECTO_JRXML);
        if (jrxmlStream == null) {
            throw new IllegalStateException("No se encontró el reporte: " + REPORTE_DIRECTO_JRXML);
        }

        JasperReport reporte = JasperCompileManager.compileReport(jrxmlStream);
        jrxmlStream.close();

        Map<String, Object> params = new HashMap<>();
        params.put("INVOICE_NUM", invoiceNum);
        params.put("INVOICE_DATE", invoiceDate);
        params.put("CLIENT_NAME", clienteNombre != null ? clienteNombre : "");
        params.put("CLIENT_PHONE", clienteTelefono != null ? clienteTelefono : "");
        params.put("CLIENT_ADDRESS", clienteDireccion != null ? clienteDireccion : "");
        params.put("CLIENT_EMAIL", clienteEmail != null ? clienteEmail : "");
        params.put("SUBTOTAL", subtotal);
        params.put("ITBIS", itbis);
        params.put("DESCUENTO", descuento);
        params.put("DELIVERY", delivery);
        params.put("TOTAL", total);
        params.put("METODO_PAGO", metodoPago);
        params.put("MONTO_RECIBIDO", montoRecibido);
        params.put("DEVUELTA", devuelta);

        Collection<Map<String, ?>> detalleRows = new ArrayList<>();
        for (ItemCarrito item : items) {
            Map<String, Object> row = new HashMap<>();
            StringBuilder prodName = new StringBuilder(item.getProducto().getNombre());
            if (!item.getExtras().isEmpty()) {
                prodName.append("\n + ").append(String.join(", ", item.getExtras()));
            }
            if (!item.getNota().isBlank()) {
                prodName.append(" (").append(item.getNota()).append(")");
            }
            row.put("nombre_producto", prodName.toString());
            row.put("cantidad", item.getCantidad());
            row.put("precio_unitario", BigDecimal.valueOf(item.getPrecioUnitario()).setScale(2, RoundingMode.HALF_UP));
            row.put("subtotal", BigDecimal.valueOf(item.getSubtotal()).setScale(2, RoundingMode.HALF_UP));
            detalleRows.add(row);
        }

        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(detalleRows);

        JasperPrint impresion = JasperFillManager.fillReport(reporte, params, dataSource);
        JasperExportManager.exportReportToPdfFile(impresion, archivoPdf.toAbsolutePath().toString());

        if (!Files.exists(archivoPdf) || Files.size(archivoPdf) == 0) {
            throw new IllegalStateException("El PDF generado está vacío.");
        }

        System.out.println("[FacturaService] Factura directa generada: " + archivoPdf.toAbsolutePath());
        return archivoPdf;
    }

    public static Path generarCajaPdf(Caja caja, double efDeclarado, double tarDeclarado, double transDeclarado,
                                       double efEsperado, double tarEsperado, double transEsperado) throws Exception {
        System.out.println("[FacturaService] Generando resumen de caja PDF...");
        Path directorio = Path.of(System.getProperty("user.dir"), "facturas");
        Files.createDirectories(directorio);
        String fileName = "CAJA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".pdf";
        Path archivoPdf = directorio.resolve(fileName);

        java.io.InputStream jrxmlStream = FacturaService.class.getResourceAsStream(REPORTE_CAJA_JRXML);
        if (jrxmlStream == null) throw new IllegalStateException("No se encontro el reporte: " + REPORTE_CAJA_JRXML);
        JasperReport reporte = JasperCompileManager.compileReport(jrxmlStream);
        jrxmlStream.close();

        BigDecimal totalDeclarado = BigDecimal.valueOf(efDeclarado + tarDeclarado + transDeclarado).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalEsperado = BigDecimal.valueOf(efEsperado + tarEsperado + transEsperado).setScale(2, RoundingMode.HALF_UP);
        BigDecimal diff = totalDeclarado.subtract(totalEsperado);

        Map<String, Object> params = new HashMap<>();
        params.put("USUARIO", caja.getUsuario());
        params.put("APERTURA", caja.getFechaApertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("CIERRE", caja.getFechaCierre() != null
            ? caja.getFechaCierre().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        params.put("MONTO_INICIAL", BigDecimal.valueOf(caja.getMontoInicial()).setScale(2, RoundingMode.HALF_UP));
        params.put("EFECTIVO_DECLARADO", BigDecimal.valueOf(efDeclarado).setScale(2, RoundingMode.HALF_UP));
        params.put("EFECTIVO_ESPERADO", BigDecimal.valueOf(efEsperado).setScale(2, RoundingMode.HALF_UP));
        params.put("TARJETA_DECLARADO", BigDecimal.valueOf(tarDeclarado).setScale(2, RoundingMode.HALF_UP));
        params.put("TARJETA_ESPERADO", BigDecimal.valueOf(tarEsperado).setScale(2, RoundingMode.HALF_UP));
        params.put("TRANSFERENCIA_DECLARADO", BigDecimal.valueOf(transDeclarado).setScale(2, RoundingMode.HALF_UP));
        params.put("TRANSFERENCIA_ESPERADO", BigDecimal.valueOf(transEsperado).setScale(2, RoundingMode.HALF_UP));
        params.put("TOTAL_DECLARADO", totalDeclarado);
        params.put("TOTAL_ESPERADO", totalEsperado);
        params.put("DIFERENCIA", diff);

        JasperPrint impresion = JasperFillManager.fillReport(reporte, params, new net.sf.jasperreports.engine.JREmptyDataSource());
        JasperExportManager.exportReportToPdfFile(impresion, archivoPdf.toAbsolutePath().toString());

        if (!Files.exists(archivoPdf) || Files.size(archivoPdf) == 0) {
            throw new IllegalStateException("El PDF de caja esta vacio.");
        }
        System.out.println("[FacturaService] Caja PDF generado: " + archivoPdf.toAbsolutePath());
        return archivoPdf;
    }

    private static String generarNumeroFactura() {
        String year = String.valueOf(LocalDate.now().getYear());
        String seq = String.format("%04d", (int) (System.currentTimeMillis() % 10000));
        return "SLS-" + year + "-" + seq;
    }

    private static BigDecimal calcularSubtotal(List<ItemCarrito> items) {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.getSubtotal();
        }
        return BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP);
    }
}
