package org.example.salsiaopf.service;

import org.example.salsiaopf.dao.DetalleVentaDAO;
import org.example.salsiaopf.dao.VentaDAO;
import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.ventas.CarritoVentas;
import org.example.salsiaopf.ventas.DatosProcesoPago;
import org.example.salsiaopf.ventas.ItemCarrito;
import org.example.salsiaopf.ventas.MetodoPago;
import org.example.salsiaopf.ventas.ResultadoProcesoVenta;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class PagoService {

    private static final DateTimeFormatter ID_FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private PagoService() {
    }

    public static String generarIdFactura() {
        return "FACT-" + LocalDateTime.now().format(ID_FMT);
    }

    public static ResultadoProcesoVenta procesarPago(CarritoVentas carrito, String emailCliente,
                                                     String metodoPago, double montoRecibido,
                                                     double devuelta) {
        return procesarPago(carrito, emailCliente, metodoPago, montoRecibido, devuelta, 1);
    }

    public static ResultadoProcesoVenta procesarPago(CarritoVentas carrito, String emailCliente,
                                                     String metodoPago, double montoRecibido,
                                                     double devuelta, int idCliente) {
        if (carrito == null || carrito.estaVacio()) {
            System.out.println("[PagoService] Error: carrito vacío o nulo.");
            return ResultadoProcesoVenta.error("El carrito está vacío.");
        }

        if (emailCliente == null || emailCliente.isBlank() || !emailCliente.contains("@")) {
            System.out.println("[PagoService] Error: correo inválido -> " + emailCliente);
            return ResultadoProcesoVenta.error("Correo del cliente inválido.");
        }

        MetodoPago metodo = MetodoPago.desdeTexto(metodoPago);
        double total = carrito.getSubtotal();
        List<ItemCarrito> items = carrito.copiarItems();
        String idFactura = generarIdFactura();

        System.out.println("[PagoService] Iniciando venta | Factura: " + idFactura
                + " | Items: " + items.size()
                + " | Total: RD$ " + String.format("%,.2f", total)
                + " | Método: " + metodo.getEtiqueta()
                + " | Email: " + emailCliente);

        DatosProcesoPago datos = new DatosProcesoPago(
                emailCliente.trim(), metodo, montoRecibido, devuelta, total);

        VentaDAO.crearTablasSiNoExisten();

        int idVenta;
        Connection conn = null;

        try {
            conn = ConexionBD.conectar();
            if (conn == null) {
                System.out.println("[PagoService] Error: no hay conexión a SQL Server.");
                return ResultadoProcesoVenta.error("No hay conexión a SQL Server.");
            }

            conn.setAutoCommit(false);
            System.out.println("[PagoService] Transacción iniciada.");

            idVenta = VentaDAO.insertarVenta(conn, idFactura, datos, idCliente);
            System.out.println("[PagoService] Venta insertada con ID: " + idVenta);

            DetalleVentaDAO.insertarDetalles(conn, idVenta, items);
            System.out.println("[PagoService] Detalle de venta insertado (" + items.size() + " líneas).");

            conn.commit();
            System.out.println("[PagoService] Transacción confirmada (commit).");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("[PagoService] Transacción revertida (rollback).");
                } catch (SQLException ex) {
                    System.out.println("[PagoService] Error en rollback: " + ex.getMessage());
                }
            }
            System.out.println("[PagoService] Transacción fallida: " + e.getMessage());
            e.printStackTrace();
            return ResultadoProcesoVenta.error("Error al guardar venta: " + e.getMessage());

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                    System.out.println("[PagoService] Conexión cerrada.");
                } catch (SQLException ignored) {
                }
            }
        }

        try {
            VentaDAO.guardarVenta(idCliente, total);
        } catch (Exception ignored) {
        }

        Path rutaPdf = null;
        String mensajeCorreo;

        try {
            System.out.println("[PagoService] Generando factura PDF para venta #" + idVenta + "...");
            rutaPdf = FacturaService.generarFacturaPdf(idVenta);
            System.out.println("[PagoService] PDF generado: " + rutaPdf);

            System.out.println("[PagoService] Enviando correo a " + emailCliente + "...");
            mensajeCorreo = EmailService.enviarFactura(emailCliente, rutaPdf, idVenta, total);
            System.out.println("[PagoService] Resultado correo: " + mensajeCorreo);
        } catch (Exception e) {
            System.out.println("[PagoService] Error PDF/Email: " + e.getMessage());
            e.printStackTrace();
            mensajeCorreo = "Venta guardada. Error PDF/correo: " + e.getMessage();
            carrito.vaciar();
            System.out.println("[PagoService] Carrito vaciado (tras error PDF/Email).");
            return new ResultadoProcesoVenta(true, idVenta, idFactura, null, false, mensajeCorreo, null);
        }

        boolean correoOk = mensajeCorreo != null && mensajeCorreo.startsWith("Correo enviado");

        carrito.vaciar();
        System.out.println("[PagoService] Carrito vaciado exitosamente. Venta #" + idVenta + " completada.");

        return new ResultadoProcesoVenta(true, idVenta, idFactura, rutaPdf, correoOk, mensajeCorreo, null);
    }

    public static ResultadoProcesoVenta procesarPago(CarritoVentas carrito, DatosProcesoPago datos) {
        if (datos == null) {
            System.out.println("[PagoService] Error: datos de pago nulos.");
            return ResultadoProcesoVenta.error("Datos de pago nulos.");
        }
        return procesarPago(
                carrito,
                datos.getEmailCliente(),
                datos.getMetodoPago().getEtiqueta(),
                datos.getMontoRecibido(),
                datos.getDevuelta()
        );
    }
}
