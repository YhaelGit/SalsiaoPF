package org.example.salsiaopf.controller;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.salsiaopf.dao.CompraDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CompraController {

    @FXML private ImageView logoImage;
    @FXML private VBox viewOrdenCompra;
    @FXML private VBox viewProveedores;
    @FXML private VBox viewRecepcion;
    @FXML private VBox viewAnalisisCompras;
    @FXML private VBox viewPagosCompra;
    @FXML private VBox viewHistorialCompras;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private TextField txtProveedorOrden;
    @FXML private TextField txtTotalOrden;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        mostrarOrdenCompra();
        animarEntrada();
    }

    private void animarEntrada() {
        ControllerUtil.animarEntrada(lblFechaActual);
    }

    private void cargarLogo() {
        ControllerUtil.cargarLogo(logoImage);
    }

    private void iniciarReloj() {
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
    }

    private void ocultarTodas() {
        VBox[] vistas = {viewOrdenCompra, viewProveedores, viewRecepcion, viewAnalisisCompras, viewPagosCompra, viewHistorialCompras};
        for (VBox vista : vistas) {
            if (vista != null) {
                vista.setVisible(false);
                vista.setManaged(false);
            }
        }
    }

    @FXML private void mostrarOrdenCompra() { ocultarTodas(); mostrarVista(viewOrdenCompra); }
    @FXML private void mostrarProveedores() { ocultarTodas(); mostrarVista(viewProveedores); }
    @FXML private void mostrarRecepcion() { ocultarTodas(); mostrarVista(viewRecepcion); }
    @FXML private void mostrarAnalisisCompras() { ocultarTodas(); mostrarVista(viewAnalisisCompras); }
    @FXML private void mostrarPagosCompra() { ocultarTodas(); mostrarVista(viewPagosCompra); }
    @FXML private void mostrarHistorialCompras() { ocultarTodas(); mostrarVista(viewHistorialCompras); }

    private void mostrarVista(VBox vista) {
        if (vista != null) {
            vista.setVisible(true);
            vista.setManaged(true);
        }
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo.");
    }

    @FXML
    private void guardarOrdenCompra() {
        String proveedor = txtProveedorOrden != null ? txtProveedorOrden.getText().trim() : "";
        if (proveedor.isEmpty()) {
            Alertas.advertencia("Validación", "Ingrese el nombre del proveedor.");
            return;
        }

        try {
            double total = txtTotalOrden != null && !txtTotalOrden.getText().isEmpty()
                    ? Double.parseDouble(txtTotalOrden.getText()) : 0.0;

            if (CompraDAO.guardarCompra(proveedor, total)) {
                Alertas.exito("Compras", "Orden de compra guardada en SQL Server.");
                limpiarOrdenCompra();
            } else {
                Alertas.error("Compras", "No se pudo guardar. Verifique tbl_COMPRA.");
            }
        } catch (NumberFormatException e) {
            Alertas.advertencia("Validación", "El total debe ser un número válido.");
        }
    }

    @FXML
    private void limpiarOrdenCompra() {
        if (txtProveedorOrden != null) txtProveedorOrden.clear();
        if (txtTotalOrden != null) txtTotalOrden.clear();
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }

    // ── Orden de Compra ──────────────────────────────────────────────
    @FXML private void nuevaOrdenCompra() { Alertas.informacion("Nueva orden", "Formulario listo para crear orden de compra."); }
    @FXML private void editarOrdenCompra() { Alertas.informacion("Editar orden", "Seleccione una orden de la tabla para editar."); }
    @FXML private void enviarOrdenCompra() { Alertas.informacion("Enviar orden", "Orden enviada al proveedor correctamente."); }
    @FXML private void cancelarOrdenCompra() {
        if (Alertas.confirmar("Cancelar orden", "¿Seguro que desea cancelar esta orden?"))
            Alertas.exito("Orden", "Orden cancelada exitosamente.");
    }
    @FXML private void agregarDetalle() { Alertas.informacion("Agregar detalle", "Nuevo detalle agregado a la orden."); }
    @FXML private void duplicarOrden() { Alertas.informacion("Duplicar", "Orden duplicada correctamente."); }
    @FXML private void marcarRecibida() { Alertas.exito("Recepción", "Orden marcada como recibida."); }

    // ── Proveedores ──────────────────────────────────────────────────
    @FXML private void nuevoProveedor() { Alertas.informacion("Nuevo proveedor", "Formulario listo para registrar proveedor."); }
    @FXML private void editarProveedor() { Alertas.informacion("Editar proveedor", "Seleccione un proveedor de la tabla."); }
    @FXML private void eliminarProveedor() {
        if (Alertas.confirmar("Eliminar", "¿Seguro de eliminar este proveedor?"))
            Alertas.exito("Proveedor", "Proveedor eliminado.");
    }
    @FXML private void guardarProveedor() { Alertas.informacion("Guardar", "Proveedor guardado correctamente."); }
    @FXML private void limpiarProveedor() { Alertas.informacion("Limpiar", "Formulario de proveedor limpiado."); }
    @FXML private void buscarProveedor() { Alertas.informacion("Buscar", "Filtrando lista de proveedores..."); }
    @FXML private void quitarSucursal() { Alertas.informacion("Sucursal", "Sucursal eliminada del proveedor."); }
    @FXML private void actualizarProveedores() { Alertas.informacion("Actualizar", "Lista de proveedores actualizada."); }

    // ── Recepción ────────────────────────────────────────────────────
    @FXML private void recibirCompra() { Alertas.informacion("Recibir", "Preparando recepción de compra..."); }
    @FXML private void confirmarRecepcion() { Alertas.exito("Recepción", "Compra recibida y confirmada exitosamente."); }
    @FXML private void rechazarRecepcion() { Alertas.advertencia("Recepción", "Compra rechazada."); }
    @FXML private void limpiarRecepcion() { Alertas.informacion("Limpiar", "Formulario de recepción limpiado."); }
    @FXML private void verOrdenesPendientes() { Alertas.informacion("Órdenes pendientes", "Mostrando órdenes pendientes de recepción."); }
    @FXML private void actualizarInventarioRecepcion() { Alertas.exito("Inventario", "Inventario actualizado desde la recepción."); }
    @FXML private void detalleRecibido() { Alertas.informacion("Detalle", "Buscando detalles de la recepción..."); }

    // ── Análisis de Compra ───────────────────────────────────────────
    @FXML private void nuevoIngredienteAnalisis() { Alertas.informacion("Nuevo ingrediente", "Formulario listo para agregar ingrediente."); }
    @FXML private void editarCosto() { Alertas.informacion("Editar costo", "Seleccione un ingrediente para editar su costo."); }
    @FXML private void actualizarAnalisis() { Alertas.informacion("Análisis", "Datos de análisis actualizados."); }
    @FXML private void refrescarAnalisis() { Alertas.informacion("Actualizar", "Vista de análisis actualizada."); }
    @FXML private void buscarAnalisis() { Alertas.informacion("Buscar", "Filtrando análisis..."); }
    @FXML private void limpiarAnalisis() { Alertas.informacion("Limpiar", "Filtros de análisis limpiados."); }
    @FXML private void verHistorialAnalisis() { Alertas.informacion("Historial", "Mostrando historial de análisis de compras."); }
    @FXML private void compararProveedores() { Alertas.informacion("Comparar", "Generando comparativa de proveedores..."); }

    // ── Pagos de Compra ──────────────────────────────────────────────
    @FXML private void nuevoPago() { Alertas.informacion("Nuevo pago", "Formulario listo para registrar pago."); }
    @FXML private void editarPago() { Alertas.informacion("Editar pago", "Seleccione un pago de la tabla."); }
    @FXML private void anularPago() {
        if (Alertas.confirmar("Anular pago", "¿Seguro de anular este pago?"))
            Alertas.exito("Pago", "Pago anulado correctamente.");
    }
    @FXML private void guardarPago() { Alertas.exito("Pago", "Pago guardado correctamente."); }
    @FXML private void limpiarPago() { Alertas.informacion("Limpiar", "Formulario de pago limpiado."); }
    @FXML private void verFacturasPendientes() { Alertas.informacion("Facturas", "Mostrando facturas pendientes de pago."); }
    @FXML private void generarReciboPago() { Alertas.informacion("Recibo", "Generando recibo de pago..."); }
    @FXML private void buscarPago() { Alertas.informacion("Buscar", "Filtrando pagos..."); }

    // ── Historial de Compras ─────────────────────────────────────────
    @FXML private void exportarHistorialCompras() { Alertas.informacion("Exportar", "Exportando historial a PDF/Excel..."); }
    @FXML private void imprimirHistorialCompras() { Alertas.informacion("Imprimir", "Enviando historial a impresora..."); }
    @FXML private void actualizarHistorialCompras() { Alertas.informacion("Actualizar", "Historial de compras actualizado."); }
    @FXML private void buscarHistorialCompras() { Alertas.informacion("Buscar", "Buscando en el historial de compras..."); }
    @FXML private void verFacturaCompra() { Alertas.informacion("Factura", "Mostrando factura de compra seleccionada."); }
    @FXML private void reimprimirCompra() { Alertas.informacion("Reimprimir", "Reimprimiendo factura de compra..."); }
    @FXML private void exportarPDFCompra() { Alertas.informacion("Exportar PDF", "Exportando factura a PDF..."); }
    @FXML private void anularCompra() {
        if (Alertas.confirmar("Anular compra", "¿Seguro de anular esta compra? Esta acción no se puede deshacer."))
            Alertas.exito("Compra", "Compra anulada exitosamente.");
    }
}
