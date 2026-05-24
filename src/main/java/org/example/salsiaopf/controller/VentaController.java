package org.example.salsiaopf.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import org.example.salsiaopf.service.FacturaService;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;
import org.example.salsiaopf.ventas.*;

import java.nio.file.Path;
import java.util.Optional;

public class VentaController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private VBox panelCategorias;
    @FXML private FlowPane gridProductos;
    @FXML private VBox panelItemsCarrito;
    @FXML private Label lblCategoriaActiva;
    @FXML private TextField txtBuscarMenu;
    @FXML private Label lblContadorItems, lblSubtotalCarrito, lblItbisCarrito, lblTotalCarrito, lblTotalCarritoHeader;
    @FXML private HBox panelEfectivoPOS;
    @FXML private TextField txtMontoPOS;
    @FXML private Label lblCambioPOS;
    @FXML private ToggleButton btnEfectivoPOS, btnTarjetaPOS, btnTransferenciaPOS;
    @FXML private Button btnCobrarPOS;

    private final CarritoVentas carrito = new CarritoVentas();
    private String categoriaActiva = CatalogoSalsiao.TODAS;

    @FXML
    public void initialize() {
        ControllerUtil.cargarLogo(logoImage);
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
        inicializarMenuDigital();
        configurarPago();
        mostrarControlVenta();
    }

    // ═══════════════════════════════════════
    // MENÚ DIGITAL
    // ═══════════════════════════════════════

    private void inicializarMenuDigital() {
        panelCategorias.getChildren().clear();
        for (String categoria : CatalogoSalsiao.CATEGORIAS) {
            Button btn = new Button(emojiCategoria(categoria) + "  " + categoria);
            btn.getStyleClass().add("ventasCatBtn");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> seleccionarCategoria(categoria));
            panelCategorias.getChildren().add(btn);
        }
        txtBuscarMenu.textProperty().addListener((obs, o, n) -> renderizarProductos());
        carrito.getItems().addListener((javafx.collections.ListChangeListener<ItemCarrito>) c ->
                Platform.runLater(this::actualizarCarritoUI));
        seleccionarCategoria(CatalogoSalsiao.TODAS);
    }

    private String emojiCategoria(String cat) {
        return switch (cat) {
            case "Hamburguesas" -> "🍔"; case "Hotdogs" -> "🌭"; case "Pizzas" -> "🍕";
            case "Tacos" -> "🌮"; case "Wraps" -> "🌯"; case "Sándwiches" -> "🥪";
            case "Pollo / Alitas" -> "🍗"; case "Picaderas" -> "🧀";
            case "Papas y extras" -> "🍟"; case "Bebidas" -> "🥤";
            case "Postres" -> "🍰"; case "Combos" -> "📦"; default -> "⭐";
        };
    }

    private void seleccionarCategoria(String categoria) {
        categoriaActiva = categoria;
        lblCategoriaActiva.setText(CatalogoSalsiao.TODAS.equals(categoria) ? "Todos los productos" : categoria);
        panelCategorias.getChildren().forEach(n -> {
            if (n instanceof Button btn) {
                boolean activo = categoria.equals(btn.getText().substring(2).trim());
                btn.getStyleClass().remove("ventasCatBtnActive");
                if (activo) btn.getStyleClass().add("ventasCatBtnActive");
            }
        });
        renderizarProductos();
    }

    private void renderizarProductos() {
        gridProductos.getChildren().clear();
        String busqueda = txtBuscarMenu.getText();
        var productos = CatalogoSalsiao.buscar(busqueda, categoriaActiva);
        if (productos.isEmpty()) {
            Label vacio = new Label("No hay productos en esta categoría");
            vacio.getStyleClass().add("ventasEmptyMsg");
            gridProductos.getChildren().add(vacio);
            return;
        }
        for (ProductoMenu producto : productos) {
            gridProductos.getChildren().add(
                    MenuDigitalUI.crearTarjetaProducto(producto, this::agregarAlCarrito));
        }
    }

    private void agregarAlCarrito(ProductoMenu producto) {
        carrito.agregar(producto);
        actualizarCarritoUI();
    }

    private void actualizarCarritoUI() {
        panelItemsCarrito.getChildren().clear();

        if (carrito.estaVacio()) {
            Label vacio = new Label("Tu carrito está vacío\nAgrega productos del menú →");
            vacio.getStyleClass().add("ventasCarritoVacio");
            panelItemsCarrito.getChildren().add(vacio);
            btnCobrarPOS.setDisable(true);
        } else {
            for (ItemCarrito item : carrito.getItems()) {
                panelItemsCarrito.getChildren().add(
                        MenuDigitalUI.crearFilaCarrito(item,
                                () -> { item.incrementar(); actualizarCarritoUI(); },
                                () -> { item.decrementar(); actualizarCarritoUI(); },
                                () -> { carrito.eliminar(item); actualizarCarritoUI(); }));
            }
            btnCobrarPOS.setDisable(false);
        }

        double subtotal = carrito.getSubtotal();
        double itbis = subtotal * 0.18;
        double total = subtotal + itbis;
        String fmtTotal = String.format("RD$ %,.2f", total);

        lblContadorItems.setText(carrito.getCantidadTotalItems() + " producto" + (carrito.getCantidadTotalItems() == 1 ? "" : "s"));
        lblTotalCarritoHeader.setText(String.format("RD$ %,.0f", total));
        lblSubtotalCarrito.setText(String.format("RD$ %,.2f", subtotal));
        lblItbisCarrito.setText(String.format("RD$ %,.2f", itbis));
        lblTotalCarrito.setText(fmtTotal);
        btnCobrarPOS.setText("💳  COBRAR " + fmtTotal);

        calcularCambio();
    }

    // ═══════════════════════════════════════
    // PAGO INLINE (sin modal)
    // ═══════════════════════════════════════

    private void configurarPago() {
        ToggleGroup grupo = new ToggleGroup();
        btnEfectivoPOS.setToggleGroup(grupo);
        btnTarjetaPOS.setToggleGroup(grupo);
        btnTransferenciaPOS.setToggleGroup(grupo);
        grupo.selectedToggleProperty().addListener((obs, o, n) -> {
            boolean esEfectivo = btnEfectivoPOS.isSelected();
            panelEfectivoPOS.setVisible(esEfectivo);
            panelEfectivoPOS.setManaged(esEfectivo);
        });
        txtMontoPOS.textProperty().addListener((obs, o, n) -> calcularCambio());
        btnCobrarPOS.setDisable(true);
    }

    private void calcularCambio() {
        if (!btnEfectivoPOS.isSelected() || txtMontoPOS == null) return;
        try {
            double recibido = txtMontoPOS.getText().isBlank() ? 0 : Double.parseDouble(txtMontoPOS.getText().trim());
            double total = carrito.getSubtotal() * 1.18;
            lblCambioPOS.setText("RD$ " + String.format("%,.2f", Math.max(0, recibido - total)));
        } catch (NumberFormatException e) {
            lblCambioPOS.setText("RD$ 0.00");
        }
    }

    @FXML
    private void cobrarPOS() {
        if (carrito.estaVacio()) { Alertas.advertencia("Carrito vacío", "Agrega productos antes de cobrar."); return; }

        String metodo = btnEfectivoPOS.isSelected() ? "Efectivo" : btnTarjetaPOS.isSelected() ? "Tarjeta" : "Transferencia";
        double subtotal = carrito.getSubtotal();
        double total = subtotal * 1.18;
        double montoRecibido = 0, devuelta = 0;

        if (btnEfectivoPOS.isSelected()) {
            try { montoRecibido = Double.parseDouble(txtMontoPOS.getText().trim()); }
            catch (NumberFormatException e) { Alertas.advertencia("Monto inválido", "Ingrese el monto recibido."); return; }
            if (montoRecibido < total) { Alertas.advertencia("Monto insuficiente", "El monto recibido debe ser al menos RD$ " + String.format("%,.2f", total)); return; }
            devuelta = montoRecibido - total;
        }

        TextInputDialog emailDialog = new TextInputDialog("cliente@email.com");
        emailDialog.setTitle("Factura");
        emailDialog.setHeaderText("Correo para enviar la factura");
        emailDialog.setContentText("Email:");
        Optional<String> emailOpt = emailDialog.showAndWait();
        if (emailOpt.isEmpty()) return;
        String email = emailOpt.get().trim();

        final double montoRecibidoFinal = montoRecibido;
        final double devueltaFinal = devuelta;

        btnCobrarPOS.setDisable(true);
        btnCobrarPOS.setText("Procesando...");

        Task<Path> task = new Task<>() {
            @Override
            protected Path call() throws Exception {
                return FacturaService.generarFacturaDirecta(
                        carrito.copiarItems(), "Cliente", "", "República Dominicana", email, metodo);
            }
        };

        task.setOnSucceeded(ev -> Platform.runLater(() -> {
            Path rutaPdf = task.getValue();
            if (rutaPdf != null) {
                FacturaDesktopUtil.abrirPdf(rutaPdf);
                FacturaDesktopUtil.imprimirPdf(rutaPdf);
            }

            StringBuilder msg = new StringBuilder();
            msg.append("Venta completada.\n\n");
            msg.append(String.format("Subtotal: RD$ %,.2f\n", subtotal));
            msg.append(String.format("ITBIS (18%%): RD$ %,.2f\n", subtotal * 0.18));
            msg.append(String.format("Total: RD$ %,.2f\n", total));
            msg.append("Método: ").append(metodo);
            if (btnEfectivoPOS.isSelected()) msg.append(String.format("\nRecibido: RD$ %,.2f\nDevuelta: RD$ %,.2f", montoRecibidoFinal, devueltaFinal));

            ButtonType btnAbrir = new ButtonType("Abrir PDF");
            ButtonType btnOk = ButtonType.OK;

            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.toString(), btnAbrir, btnOk);
            alert.setTitle("Venta completada");
            alert.setHeaderText("✅ Cobro exitoso — Salsiao");
            Optional<ButtonType> opcion = alert.showAndWait();
            if (rutaPdf != null && opcion.isPresent() && opcion.get() == btnAbrir) {
                FacturaDesktopUtil.abrirPdf(rutaPdf);
            }

            carrito.vaciar();
            actualizarCarritoUI();
            btnCobrarPOS.setDisable(false);
            btnCobrarPOS.setText("💳  COBRAR");
        }));

        task.setOnFailed(ev -> Platform.runLater(() -> {
            btnCobrarPOS.setDisable(false);
            btnCobrarPOS.setText("💳  COBRAR");
            Throwable err = task.getException();
            Alertas.error("Error", err != null ? err.getMessage() : "Error al procesar venta.");
            if (err != null) err.printStackTrace();
        }));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ═══════════════════════════════════════
    // FACTURA DIRECTA (sin cobro)
    // ═══════════════════════════════════════

    @FXML
    private void generarFacturaDirecta() {
        if (carrito.estaVacio()) { Alertas.advertencia("Carrito vacío", "Agrega productos al carrito."); return; }

        TextInputDialog dialogNombre = new TextInputDialog("Cliente Salsiao");
        dialogNombre.setTitle("Datos del cliente"); dialogNombre.setHeaderText("Nombre:"); dialogNombre.setContentText("Nombre:");
        Optional<String> nombreOpt = dialogNombre.showAndWait();
        if (nombreOpt.isEmpty() || nombreOpt.get().isBlank()) return;

        TextInputDialog dialogTel = new TextInputDialog("(809) 555-0000");
        dialogTel.setTitle("Datos del cliente"); dialogTel.setHeaderText("Teléfono:"); dialogTel.setContentText("Teléfono:");
        Optional<String> telOpt = dialogTel.showAndWait();
        if (telOpt.isEmpty()) return;

        TextInputDialog dialogDir = new TextInputDialog("República Dominicana");
        dialogDir.setTitle("Datos del cliente"); dialogDir.setHeaderText("Dirección:"); dialogDir.setContentText("Dirección:");
        Optional<String> dirOpt = dialogDir.showAndWait();
        if (dirOpt.isEmpty()) return;

        TextInputDialog dialogEmail = new TextInputDialog("cliente@email.com");
        dialogEmail.setTitle("Datos del cliente"); dialogEmail.setHeaderText("Correo:"); dialogEmail.setContentText("Email:");
        Optional<String> emailOpt = dialogEmail.showAndWait();
        if (emailOpt.isEmpty()) return;

        String nombre = nombreOpt.get().trim(), telefono = telOpt.get().trim();
        String direccion = dirOpt.get().trim(), email = emailOpt.get().trim();

        Task<Path> task = new Task<>() {
            @Override protected Path call() throws Exception {
                return FacturaService.generarFacturaDirecta(carrito.copiarItems(), nombre, telefono, direccion, email, null);
            }
        };

        task.setOnSucceeded(ev -> Platform.runLater(() -> {
            Path rutaPdf = task.getValue();
            if (rutaPdf != null) FacturaDesktopUtil.abrirPdf(rutaPdf);

            double subtotal = carrito.getSubtotal();
            StringBuilder msg = new StringBuilder();
            msg.append("Factura generada.\n\n");
            msg.append(String.format("Subtotal: RD$ %,.2f\n", subtotal));
            msg.append(String.format("ITBIS (18%%): RD$ %,.2f\n", subtotal * 0.18));
            msg.append(String.format("Total: RD$ %,.2f\n", subtotal * 1.18));
            msg.append("\nPDF: ").append(rutaPdf.toAbsolutePath());

            ButtonType btnAbrir = new ButtonType("Abrir PDF");
            ButtonType btnImprimir = new ButtonType("Imprimir");
            ButtonType btnOk = ButtonType.OK;

            Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.toString(), btnAbrir, btnImprimir, btnOk);
            alert.setTitle("Factura"); alert.setHeaderText("✅ Factura generada — Salsiao RD");
            Optional<ButtonType> opcion = alert.showAndWait();
            if (rutaPdf != null && opcion.isPresent()) {
                if (opcion.get() == btnAbrir) FacturaDesktopUtil.abrirPdf(rutaPdf);
                else if (opcion.get() == btnImprimir) FacturaDesktopUtil.imprimirPdf(rutaPdf);
            }
        }));

        task.setOnFailed(ev -> Platform.runLater(() -> {
            Throwable err = task.getException();
            Alertas.error("Error", err != null ? err.getMessage() : "Error desconocido.");
            if (err != null) err.printStackTrace();
        }));

        Thread hilo = new Thread(task);
        hilo.setDaemon(true);
        hilo.start();
    }

    // ═══════════════════════════════════════
    // NAVEGACIÓN
    // ═══════════════════════════════════════

    @FXML private VBox viewProductos, viewControlVenta, viewOrdenActual, viewPago, viewFacturacion;
    @FXML private VBox viewPedidosPendientes, viewDelivery, viewMesas, viewHistorialVentas, viewCaja;

    private void ocultarTodas() {
        for (VBox v : new VBox[]{viewProductos, viewControlVenta, viewOrdenActual, viewPago,
                viewFacturacion, viewPedidosPendientes, viewDelivery, viewMesas, viewHistorialVentas, viewCaja}) {
            if (v != null) { v.setVisible(false); v.setManaged(false); }
        }
    }

    private void mostrar(VBox v) { if (v != null) { v.setVisible(true); v.setManaged(true); } }

    @FXML private void mostrarControlVenta() { ocultarTodas(); mostrar(viewControlVenta); renderizarProductos(); }
    @FXML private void mostrarProductos() { ocultarTodas(); mostrar(viewProductos); }
    @FXML private void mostrarOrdenActual() { ocultarTodas(); mostrar(viewOrdenActual); }
    @FXML private void mostrarPago() { ocultarTodas(); mostrar(viewPago); actualizarCarritoUI(); }
    @FXML private void mostrarFacturacion() { ocultarTodas(); mostrar(viewFacturacion); }
    @FXML private void mostrarPedidosPendientes() { ocultarTodas(); mostrar(viewPedidosPendientes); }
    @FXML private void mostrarDelivery() { ocultarTodas(); mostrar(viewDelivery); }
    @FXML private void mostrarMesas() { ocultarTodas(); mostrar(viewMesas); }
    @FXML private void mostrarHistorialVentas() { ocultarTodas(); mostrar(viewHistorialVentas); }
    @FXML private void mostrarCaja() { ocultarTodas(); mostrar(viewCaja); }
    @FXML private void mostrarNotificaciones() { Alertas.advertencia("Notificaciones", "Módulo en desarrollo."); }
    @FXML private void actualizarPagoView() { actualizarCarritoUI(); }
    @FXML private void cancelarPagoView() { mostrarControlVenta(); }
    @FXML private void cancelarVenta() { carrito.vaciar(); mostrarControlVenta(); }
    @FXML private void imprimirTicket() { cobrarPOS(); }
    @FXML private void vaciarCarrito() { carrito.vaciar(); actualizarCarritoUI(); }
    @FXML private void volverMenu(ActionEvent e) { Navegacion.volverCentroSistema(e); }
    @FXML private void salirSistema(ActionEvent e) { SessionManager.getInstance().cerrarSesion(); System.exit(0); }
}
