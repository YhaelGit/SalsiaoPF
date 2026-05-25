package org.example.salsiaopf.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import org.example.salsiaopf.dao.VentaDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.RoleGuard;
import org.example.salsiaopf.util.SessionManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.text.NumberFormat;

public class CentroSistemaController {

    @FXML private javafx.scene.image.ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual, lblUsuarioTop, lblRolTop, lblBienvenida, lblUsuarioCard, lblRolCard;
    @FXML private Button btnNotificaciones, btnNavVentas, btnNavCompras, btnNavClientes, btnNavInventario, btnNavEmpleados, btnNavMantenimiento;

    @FXML private BarChart<String, Number> chartVentasSemana;
    @FXML private Label lblVentasHoy, lblCantVentasHoy, lblTotalSemana;

    @FXML private Label lblComprasPendientes, lblTotalComprasPendientes, lblClientesRegistrados;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));

    @FXML
    private void initialize() {
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);
        cargarLogo();
        iniciarReloj();
        mostrarDatosSesion();
        aplicarPermisosModulos();
        animarEntrada();
        cargarDashboard();
    }

    private void cargarLogo() {
        ControllerUtil.cargarLogo(logoImage);
    }

    private void iniciarReloj() {
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
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
    }

    private void animarEntrada() {
        ControllerUtil.animarEntrada(lblBienvenida);
    }

    private void configurarBotonModulo(Button boton, String modulo) {
        if (boton == null) return;
        boolean permitido = RoleGuard.tienePermiso(modulo);
        boton.setDisable(!permitido);
        if (!permitido) {
            if (!boton.getStyleClass().contains("sideButtonDenied")) boton.getStyleClass().add("sideButtonDenied");
            boton.setTooltip(new javafx.scene.control.Tooltip("Acceso denegado para tu rol"));
        }
    }

    private void cargarDashboard() {
        double totalHoy = VentaDAO.obtenerTotalVentasHoy();
        int cantHoy = VentaDAO.obtenerCantidadVentasHoy();
        if (lblVentasHoy != null) lblVentasHoy.setText(fmt.format(totalHoy));
        if (lblCantVentasHoy != null) lblCantVentasHoy.setText(cantHoy + " ventas");

        int clientes = VentaDAO.obtenerCantidadClientes();
        if (lblClientesRegistrados != null) lblClientesRegistrados.setText(String.valueOf(clientes));

        var ventasSemana = VentaDAO.obtenerVentasPorSemana();
        double totalSemana = 0;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        DateTimeFormatter diaFmt = DateTimeFormatter.ofPattern("EEE", new Locale("es", "DO"));
        for (var e : ventasSemana.entrySet()) {
            String label = e.getKey().format(diaFmt);
            series.getData().add(new XYChart.Data<>(label, e.getValue()));
            totalSemana += e.getValue();
        }
        if (chartVentasSemana != null) {
            chartVentasSemana.getData().clear();
            chartVentasSemana.getData().add(series);
        }
        if (lblTotalSemana != null) lblTotalSemana.setText("Total semana: " + fmt.format(totalSemana));


    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.informacion("Notificaciones", "Panel de notificaciones próximamente disponible.");
    }

    @FXML
    private void irPanelNotificaciones(ActionEvent event) { mostrarNotificaciones(); }

    @FXML
    private void irPanelInventario(ActionEvent event) {
        if (!RoleGuard.permitir("inventario")) return;
        Navegacion.abrirInventario(event);
    }

    @FXML
    private void mostrarGeneral(ActionEvent event) {
        cargarDashboard();
        Alertas.informacion("Panel General", "Datos actualizados.");
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
    private void actualizarDashboard() {
        cargarDashboard();
        Alertas.exito("Dashboard", "Panel actualizado correctamente.");
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        System.exit(0);
    }
}
