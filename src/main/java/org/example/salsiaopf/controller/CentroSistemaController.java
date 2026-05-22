package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.RoleGuard;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.example.salsiaopf.util.SessionManager;

public class CentroSistemaController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private Label lblUsuarioTop;
    @FXML private Label lblRolTop;
    @FXML private Label lblBienvenida;
    @FXML private Label lblUsuarioCard;
    @FXML private Label lblRolCard;
    @FXML private Button btnNavVentas;
    @FXML private Button btnNavCompras;
    @FXML private Button btnNavClientes;
    @FXML private Button btnNavInventario;
    @FXML private Button btnNavEmpleados;
    @FXML private Button btnNavMantenimiento;
    @FXML private Button btnNavReportes;
    @FXML private Button btnNavConfiguracion;

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        actualizarNotificaciones();
        mostrarDatosSesion();
        aplicarPermisosModulos();
        animarEntrada();
    }

    private int cantidadNotificaciones = 3;

    private void cargarLogo() {
        ControllerUtil.cargarLogo(logoImage);
    }

    private void cambiarEscena(String archivoFXML, String titulo, ActionEvent event) {
        Navegacion.cambiarEscena(event, archivoFXML, titulo);
    }

    private void iniciarReloj() {
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
    }

    private void actualizarNotificaciones() {
        btnNotificaciones.setText("🔔 " + cantidadNotificaciones);
    }

    private void mostrarDatosSesion() {
        var usuario = SessionManager.getInstance().getUsuarioActivo();
        if (usuario == null) return;

        String nombre = usuario.getNombre();
        String rol = usuario.getRol();

        if (lblUsuarioTop != null) lblUsuarioTop.setText(nombre);
        if (lblRolTop != null) lblRolTop.setText(rol);
        if (lblBienvenida != null) lblBienvenida.setText("Bienvenido, " + nombre + " 👋");
        if (lblUsuarioCard != null) lblUsuarioCard.setText(nombre);
        if (lblRolCard != null) lblRolCard.setText("Rol: " + rol);
    }

    private void aplicarPermisosModulos() {
        configurarBotonModulo(btnNavVentas, "ventas");
        configurarBotonModulo(btnNavCompras, "compras");
        configurarBotonModulo(btnNavClientes, "clientes");
        configurarBotonModulo(btnNavInventario, "inventario");
        configurarBotonModulo(btnNavEmpleados, "empleados");
        configurarBotonModulo(btnNavMantenimiento, "mantenimiento");
        configurarBotonModulo(btnNavReportes, "reportes");
        if (btnNavConfiguracion != null) {
            configurarBotonModulo(btnNavConfiguracion, "mantenimiento");
        }
    }

    private void animarEntrada() {
        ControllerUtil.animarEntrada(lblBienvenida);
    }

    private void configurarBotonModulo(Button boton, String modulo) {
        if (boton == null) return;

        boolean permitido = RoleGuard.tienePermiso(modulo);
        boton.setDisable(!permitido);

        if (!permitido) {
            if (!boton.getStyleClass().contains("sideButtonDenied")) {
                boton.getStyleClass().add("sideButtonDenied");
            }
            boton.setTooltip(new javafx.scene.control.Tooltip("Acceso denegado para tu rol"));
        }
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.informacion("Notificaciones",
                "Tienes " + cantidadNotificaciones + " notificaciones pendientes.\n"
                + "Panel de notificaciones próximamente disponible.");
    }

    @FXML
    private void mostrarGeneral(ActionEvent event) {
        Alertas.informacion("Panel General",
                "Bienvenido al panel general de Salsiao.\n"
                + "Selecciona un módulo en el menú lateral para comenzar.");
    }

    @FXML
    private void irNuevaVenta(ActionEvent event) {
        if (!RoleGuard.permitir("ventas")) return;
        Navegacion.abrirVentas(event);
    }

    @FXML
    private void irNuevoCliente(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void irNuevaCompra(ActionEvent event) {
        if (!RoleGuard.permitir("compras")) return;
        Navegacion.abrirCompras(event);
    }

    @FXML
    private void irNuevoProducto(ActionEvent event) {
        if (!RoleGuard.permitir("inventario")) return;
        Navegacion.abrirInventario(event);
    }

    @FXML
    private void irNuevaReserva(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void irReporteVentas(ActionEvent event) {
        if (!RoleGuard.permitir("reportes")) return;
        Alertas.informacion("Reportes de ventas",
                "Módulo de reportes en desarrollo.\n"
                + "Próximamente: reportes gráficos, exportación a Excel y PDF.");
    }

    @FXML
    private void abrirVentas(ActionEvent event) {
        if (!RoleGuard.permitir("ventas")) return;
        Navegacion.abrirVentas(event);
    }

    @FXML
    private void abrirCompras(ActionEvent event) {
        if (!RoleGuard.permitir("compras")) return;
        Navegacion.abrirCompras(event);
    }

    @FXML
    private void abrirClientes(ActionEvent event) {
        if (!RoleGuard.permitir("clientes")) return;
        Navegacion.abrirClientes(event);
    }

    @FXML
    private void abrirInventario(ActionEvent event) {
        if (!RoleGuard.permitir("inventario")) return;
        Navegacion.abrirInventario(event);
    }

    @FXML
    private void abrirEmpleados(ActionEvent event) {
        if (!RoleGuard.permitir("empleados")) return;
        Navegacion.abrirEmpleados(event);
    }

    @FXML
    private void abrirMantenimiento(ActionEvent event) {
        if (!RoleGuard.permitir("mantenimiento")) return;
        Navegacion.abrirMantenimiento(event);
    }

    @FXML
    private void abrirReportes(ActionEvent event) {
        if (!RoleGuard.permitir("reportes")) return;
        Alertas.informacion("Reportes",
                "Módulo de reportes en desarrollo.\n"
                + "Próximamente: reportes de ventas, compras, inventario y más.");
    }

    @FXML
    private void abrirConfiguracion(ActionEvent event) {
        Alertas.informacion("Configuración",
                "Módulo de configuración en desarrollo.\n"
                + "Próximamente: ajustes del sistema, preferencias y personalización.");
    }

    @FXML
    private void actualizarDashboard() {
        actualizarNotificaciones();
        mostrarDatosSesion();
        Alertas.exito("Dashboard", "Panel actualizado correctamente.");
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }
}
