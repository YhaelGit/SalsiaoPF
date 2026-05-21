package org.example.salsiaopf.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import org.example.salsiaopf.database.ConexionBD;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
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

        Exception jasperError = null;

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
            jasperError = e;
            System.out.println("[FacturaService] JRXML externo fallo: " + e.getMessage());
        }

        // INTENTO 2: Reporte en memoria
        System.out.println("[FacturaService] Intento 2: Construyendo reporte en memoria...");
        try {
            return generarPdfEnMemoria(idVenta, archivoPdf);
        } catch (Exception e) {
            System.out.println("[FacturaService] Fallback en memoria fallo: " + e.getMessage());
            e.printStackTrace();
            if (jasperError != null) {
                System.out.println("[FacturaService] Error original JRXML: " + jasperError.getMessage());
            }
            throw new Exception("No se pudo generar la factura PDF: " + e.getMessage(), e);
        }
    }

    // Clase interna para detalles
    public static class DetalleFactura {
        private final String nombreProducto;
        private final Integer cantidad;
        private final BigDecimal precioUnitario;
        private final BigDecimal subtotal;
        public DetalleFactura(String nombreProducto, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
            this.nombreProducto = nombreProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }
        public String getNombreProducto() { return nombreProducto; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
    }

    private static Path generarPdfEnMemoria(int idVenta, Path archivoPdf) throws Exception {
        System.out.println("[FacturaService] Obteniendo datos de venta #" + idVenta + " desde BD...");

        Map<String, Object> datosVenta = new HashMap<>();
        List<DetalleFactura> detalles = new ArrayList<>();

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) throw new IllegalStateException("Sin conexion a SQL Server.");

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id_venta, id_factura, fecha_venta, total, metodo_pago, monto_recibido, devuelta, email_cliente FROM tbl_VENTA_POS WHERE id_venta = ?")) {
                ps.setInt(1, idVenta);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        datosVenta.put("id_venta", rs.getInt("id_venta"));
                        datosVenta.put("id_factura", rs.getString("id_factura"));
                        datosVenta.put("fecha_venta", rs.getTimestamp("fecha_venta"));
                        datosVenta.put("total", rs.getBigDecimal("total"));
                        datosVenta.put("metodo_pago", rs.getString("metodo_pago"));
                        datosVenta.put("monto_recibido", rs.getBigDecimal("monto_recibido"));
                        datosVenta.put("devuelta", rs.getBigDecimal("devuelta"));
                        datosVenta.put("email_cliente", rs.getString("email_cliente"));
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
                        detalles.add(new DetalleFactura(
                                rs.getString("nombre_producto"),
                                rs.getInt("cantidad"),
                                rs.getBigDecimal("precio_unitario"),
                                rs.getBigDecimal("subtotal")
                        ));
                    }
                }
            }
        }

        System.out.println("[FacturaService] Datos: " + detalles.size() + " detalles.");
        if (detalles.isEmpty()) throw new IllegalStateException("Venta sin detalles.");

        JasperDesign design = new JasperDesign();
        design.setName("factura_salsiao_memoria");
        design.setPageWidth(595);
        design.setPageHeight(842);
        design.setColumnWidth(555);
        design.setLeftMargin(20);
        design.setRightMargin(20);
        design.setTopMargin(20);
        design.setBottomMargin(20);

        // Campos
        String[] fnames = {"nombreProducto", "cantidad", "precioUnitario", "subtotal"};
        String[] fclasses = {"java.lang.String", "java.lang.Integer", "java.math.BigDecimal", "java.math.BigDecimal"};
        for (int i = 0; i < fnames.length; i++) {
            JRDesignField f = new JRDesignField();
            f.setName(fnames[i]);
            f.setValueClassName(fclasses[i]);
            design.addField(f);
        }

        Float font10 = 10f;
        Float font11 = 11f;
        Float font9 = 9f;
        Float font14 = 14f;
        Float font24 = 24f;

        // Title band
        JRDesignBand title = new JRDesignBand();
        title.setHeight(100);

        JRDesignStaticText logo = new JRDesignStaticText();
        logo.setX(0); logo.setY(10); logo.setWidth(555); logo.setHeight(35);
        logo.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_CENTER);
        logo.setFontSize(font24); logo.setBold(true); logo.setText("SALSIAO");
        title.addElement(logo);

        JRDesignStaticText sub = new JRDesignStaticText();
        sub.setX(0); sub.setY(45); sub.setWidth(555); sub.setHeight(18);
        sub.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_CENTER);
        sub.setFontSize(font11); sub.setText("Fast Food - Factura de Venta");
        title.addElement(sub);

        JRDesignTextField fac = textField(0, 65, 280, 16, font10, "\"Factura: \" + $P{id_factura}", null);
        title.addElement(fac);

        JRDesignTextField ven = textField(280, 65, 275, 16, font10, "\"Venta #\" + $P{id_venta}", JRAlignment.HORIZONTAL_ALIGN_RIGHT);
        title.addElement(ven);

        JRDesignTextField fec = textField(0, 82, 280, 14, font9, "$P{fecha_venta}", null);
        title.addElement(fec);

        JRDesignTextField em = textField(280, 82, 275, 14, font9, "$P{email_cliente}", JRAlignment.HORIZONTAL_ALIGN_RIGHT);
        title.addElement(em);

        design.setTitle(title);

        // Column header
        JRDesignBand colHeader = new JRDesignBand();
        colHeader.setHeight(22);
        String[] ch = {" Producto", "Cant.", "Precio", "Subtotal"};
        int[] cx = {0, 240, 295, 415};
        int[] cw = {240, 55, 120, 140};
        for (int i = 0; i < ch.length; i++) {
            JRDesignStaticText st = new JRDesignStaticText();
            st.setX(cx[i]); st.setY(0); st.setWidth(cw[i]); st.setHeight(20);
            st.setText(ch[i]); st.setFontSize(font10); st.setBold(true);
            if (i >= 2) st.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_RIGHT);
            else if (i == 1) st.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_CENTER);
            colHeader.addElement(st);
        }
        design.setColumnHeader(colHeader);

        // Detail
        JRDesignDetail detail = new JRDesignDetail();
        JRDesignBand detailBand = new JRDesignBand();
        detailBand.setHeight(20);
        String[] df = {"nombreProducto", "cantidad", "precioUnitario", "subtotal"};
        int[] dx = {0, 240, 295, 415};
        int[] dw = {240, 55, 120, 140};
        for (int i = 0; i < df.length; i++) {
            JRDesignTextField tf = textField(dx[i], 0, dw[i], 18, font10, "$F{" + df[i] + "}",
                    i >= 2 ? JRAlignment.HORIZONTAL_ALIGN_RIGHT : i == 1 ? JRAlignment.HORIZONTAL_ALIGN_CENTER : null);
            detailBand.addElement(tf);
        }
        detail.addBand(detailBand);
        design.setDetail(detail);

        // Summary
        JRDesignBand summary = new JRDesignBand();
        summary.setHeight(80);

        JRDesignLine line = new JRDesignLine();
        line.setX(0); line.setY(5); line.setWidth(555); line.setHeight(1);
        summary.addElement(line);

        JRDesignStaticText tl = new JRDesignStaticText();
        tl.setX(300); tl.setY(15); tl.setWidth(120); tl.setHeight(18);
        tl.setText("TOTAL RD$:"); tl.setFontSize(font11); tl.setBold(true);
        tl.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_RIGHT);
        summary.addElement(tl);

        JRDesignTextField tv = textField(420, 15, 135, 18, font14, "$P{total}", JRAlignment.HORIZONTAL_ALIGN_RIGHT);
        tv.setBold(true);
        summary.addElement(tv);

        JRDesignTextField mp = textField(300, 38, 255, 16, font9, "\"Metodo: \" + $P{metodo_pago}", JRAlignment.HORIZONTAL_ALIGN_RIGHT);
        summary.addElement(mp);

        JRDesignStaticText gr = new JRDesignStaticText();
        gr.setX(0); gr.setY(65); gr.setWidth(555); gr.setHeight(14);
        gr.setText("Gracias por su compra - Salsiao"); gr.setFontSize(font9); gr.setItalic(true);
        gr.setHorizontalAlignment(JRAlignment.HORIZONTAL_ALIGN_CENTER);
        summary.addElement(gr);

        design.setSummary(summary);

        // Parametros
        Map<String, Object> params = new HashMap<>();
        params.put("id_venta", datosVenta.get("id_venta"));
        params.put("id_factura", datosVenta.get("id_factura"));
        params.put("fecha_venta", datosVenta.get("fecha_venta"));
        params.put("total", datosVenta.get("total"));
        params.put("metodo_pago", datosVenta.get("metodo_pago"));
        params.put("monto_recibido", datosVenta.get("monto_recibido"));
        params.put("devuelta", datosVenta.get("devuelta"));
        params.put("email_cliente", datosVenta.get("email_cliente"));

        System.out.println("[FacturaService] Compilando reporte en memoria...");
        JasperReport reporte = JasperCompileManager.compileReport(design);
        System.out.println("[FacturaService] Llenando con " + detalles.size() + " detalles...");
        JRBeanCollectionDataSource ds = new JRBeanCollectionDataSource(detalles);
        JasperPrint impresion = JasperFillManager.fillReport(reporte, params, ds);
        System.out.println("[FacturaService] Exportando PDF: " + archivoPdf);
        JasperExportManager.exportReportToPdfFile(impresion, archivoPdf.toAbsolutePath().toString());

        if (!Files.exists(archivoPdf)) throw new IllegalStateException("PDF no se genero: " + archivoPdf);
        System.out.println("[FacturaService] PDF generado: " + archivoPdf + " (" + Files.size(archivoPdf) + " bytes)");
        return archivoPdf;
    }

    private static JRDesignTextField textField(int x, int y, int w, int h, Float fontSize, String expression, Integer alignment) {
        JRDesignTextField tf = new JRDesignTextField();
        tf.setX(x); tf.setY(y); tf.setWidth(w); tf.setHeight(h);
        tf.setFontSize(fontSize);
        if (alignment != null) tf.setHorizontalAlignment(alignment);
        JRDesignExpression expr = new JRDesignExpression();
        expr.setText(expression);
        tf.setExpression(expr);
        return tf;
    }
}
