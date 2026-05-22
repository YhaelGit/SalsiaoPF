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
}
