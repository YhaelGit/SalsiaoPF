package org.example.salsiaopf.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import org.example.salsiaopf.database.ConexionBD;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FacturaService {

    private static final String REPORTE_JRXML = "/reports/factura_salsiao.jrxml";

    private FacturaService() {
    }

    public static Path generarFacturaPdf(int idVenta) throws Exception {
        System.out.println("[FacturaService] === INICIO generacion factura para venta #" + idVenta + " ===");

        Path directorio = Path.of(System.getProperty("user.dir"), "facturas");
        Files.createDirectories(directorio);
        Path archivoPdf = directorio.resolve("FACTURA-" + idVenta + ".pdf");

        // INTENTO 1: JRXML externo
        try {
            System.out.println("[FacturaService] Intento 1: JRXML externo " + REPORTE_JRXML);
            java.io.InputStream jrxmlStream = FacturaService.class.getResourceAsStream(REPORTE_JRXML);
            if (jrxmlStream != null) {
                String contenido = new String(jrxmlStream.readAllBytes());
                jrxmlStream.close();
                boolean tieneBands = contenido.contains("<title>") || contenido.contains("<detail>")
                        || contenido.contains("<pageHeader>") || contenido.contains("<columnHeader>");
                if (tieneBands) {
                    System.out.println("[FacturaService] JRXML tiene bands, compilando...");
                    JasperReport reporte = JasperCompileManager.compileReport(
                            FacturaService.class.getResourceAsStream(REPORTE_JRXML));
                    Map<String, Object> parametros = new HashMap<>();
                    parametros.put("ID_VENTA", idVenta);
                    try (Connection conn = ConexionBD.conectar()) {
                        if (conn == null) throw new IllegalStateException("Sin conexion a SQL Server.");
                        JasperPrint impresion = JasperFillManager.fillReport(reporte, parametros, conn);
                        JasperExportManager.exportReportToPdfFile(impresion, archivoPdf.toAbsolutePath().toString());
                    }
                    if (Files.exists(archivoPdf) && Files.size(archivoPdf) > 100) {
                        System.out.println("[FacturaService] PDF generado con JRXML externo: " + archivoPdf);
                        return archivoPdf;
                    }
                    System.out.println("[FacturaService] JRXML externo produjo PDF vacio, usando fallback.");
                } else {
                    System.out.println("[FacturaService] JRXML externo sin bands, usando fallback.");
                }
            } else {
                System.out.println("[FacturaService] JRXML externo no encontrado, usando fallback.");
            }
        } catch (Exception e) {
            System.out.println("[FacturaService] JRXML externo fallo: " + e.getMessage());
            e.printStackTrace();
        }

        // INTENTO 2: Fallback - factura de texto
        System.out.println("[FacturaService] Intento 2: Generando factura de texto...");
        return generarFacturaTexto(idVenta, directorio);
    }

    private static Path generarFacturaTexto(int idVenta, Path directorio) throws Exception {
        System.out.println("[FacturaService] Obteniendo datos de venta #" + idVenta + " desde BD...");

        String idFactura = "";
        String fechaVenta = "";
        String total = "0.00";
        String metodoPago = "";
        String montoRecibido = "";
        String devuelta = "";
        String emailCliente = "";
        List<String[]> detalles = new ArrayList<>();

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) throw new IllegalStateException("Sin conexion a SQL Server.");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_venta, id_factura, fecha_venta, total, metodo_pago, monto_recibido, devuelta, email_cliente FROM tbl_VENTA_POS WHERE id_venta = ?")) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idFactura = rs.getString("id_factura");
                        fechaVenta = String.valueOf(rs.getTimestamp("fecha_venta"));
                        total = rs.getBigDecimal("total").toString();
                        metodoPago = rs.getString("metodo_pago");
                        montoRecibido = rs.getString("monto_recibido");
                        devuelta = rs.getString("devuelta");
                        emailCliente = rs.getString("email_cliente");
                    } else {
                        throw new IllegalStateException("Venta #" + idVenta + " no encontrada.");
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT nombre_producto, cantidad, precio_unitario, subtotal FROM tbl_DETALLE_VENTA_POS WHERE id_venta = ? ORDER BY id_detalle")) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        detalles.add(new String[]{
                                rs.getString("nombre_producto"),
                                String.valueOf(rs.getInt("cantidad")),
                                rs.getBigDecimal("precio_unitario").toString(),
                                rs.getBigDecimal("subtotal").toString()
                        });
                    }
                }
            }
        }

        if (detalles.isEmpty()) throw new IllegalStateException("Venta sin detalles.");

        Path archivoTxt = directorio.resolve("FACTURA-" + idVenta + ".txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(archivoTxt.toFile()))) {
            pw.println("========================================");
            pw.println("            S A L S I A O");
            pw.println("       Fast Food - Factura de Venta");
            pw.println("========================================");
            pw.println("Factura: " + idFactura);
            pw.println("Venta #: " + idVenta);
            pw.println("Fecha: " + fechaVenta);
            pw.println("Email: " + emailCliente);
            pw.println("----------------------------------------");
            pw.printf("%-25s %5s %10s %12s%n", "Producto", "Cant.", "Precio", "Subtotal");
            pw.println("----------------------------------------");
            for (String[] d : detalles) {
                pw.printf("%-25s %5s %10s %12s%n", d[0], d[1], d[2], d[3]);
            }
            pw.println("========================================");
            pw.printf("TOTAL RD$: %s%n", total);
            pw.println("----------------------------------------");
            pw.println("Metodo: " + metodoPago);
            if (montoRecibido != null && !montoRecibido.equals("null")) {
                pw.println("Recibido: " + montoRecibido);
            }
            if (devuelta != null && !devuelta.equals("null")) {
                pw.println("Devuelta: " + devuelta);
            }
            pw.println("========================================");
            pw.println("  Gracias por su compra - Salsiao");
            pw.println("========================================");
        }

        System.out.println("[FacturaService] Factura de texto generada: " + archivoTxt);
        return archivoTxt;
    }
}
