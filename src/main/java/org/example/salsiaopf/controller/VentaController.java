package org.example.salsiaopf.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;
import org.example.salsiaopf.ventas.CarritoVentas;
import org.example.salsiaopf.ventas.CatalogoSalsiao;
import org.example.salsiaopf.ventas.ItemCarrito;
import org.example.salsiaopf.ventas.MenuDigitalUI;
import org.example.salsiaopf.ventas.ProductoMenu;
import org.example.salsiaopf.ventas.FacturaDesktopUtil;
import org.example.salsiaopf.ventas.ResultadoProcesoVenta;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class VentaController {

    // ── Vistas existentes del módulo (sin eliminar) ─────────────────────
    @FXML private VBox viewProductos;
    @FXML private VBox viewControlVenta;
    @FXML private VBox viewOrdenActual;
    @FXML private VBox viewPago;
    @FXML private VBox viewFacturacion;
    @FXML private VBox viewPedidosPendientes;
    @FXML private VBox viewDelivery;
    @FXML private VBox viewMesas;
    @FXML private VBox viewHistorialVentas;
    @FXML private VBox viewCaja;

    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private ImageView logoImage;
    @FXML private TextField txtMontoRecibido;
    @FXML private Label lblCambio;
    @FXML private Label lblTotalPago;

    // ── Menú digital ────────────────────────────────────────────────────
    @FXML private VBox panelCategorias;
    @FXML private FlowPane gridProductos;
    @FXML private VBox panelItemsCarrito;
    @FXML private Label lblCategoriaActiva;
    @FXML private TextField txtBuscarMenu;
    @FXML private Label lblContadorItems;
    @FXML private Label lblTotalCarritoHeader;
    @FXML private Label lblSubtotalCarrito;
    @FXML private Label lblTotalCarrito;

    private final CarritoVentas carrito = new CarritoVentas();
    private String categoriaActiva = CatalogoSalsiao.TODAS;
    private double totalVentaActual = 0.0;

    @FXML
    public void initialize() {
        cargarLogo();
        iniciarReloj();
        inicializarMenuDigital();
        mostrarControlVenta();

        if (txtMontoRecibido != null) {
            txtMontoRecibido.textProperty().addListener((obs, oldValue, newValue) -> calcularCambio());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // MENÚ DIGITAL
    // ═══════════════════════════════════════════════════════════════════

    private void inicializarMenuDigital() {
        if (panelCategorias == null) return;

        panelCategorias.getChildren().clear();
        for (String categoria : CatalogoSalsiao.CATEGORIAS) {
            Button btn = crearBotonCategoria(categoria);
            panelCategorias.getChildren().add(btn);
        }

        if (txtBuscarMenu != null) {
            txtBuscarMenu.textProperty().addListener((obs, o, n) -> renderizarProductos());
        }

        carrito.getItems().addListener((javafx.collections.ListChangeListener<ItemCarrito>) c -> {
            Platform.runLater(this::actualizarCarritoUI);
        });

        seleccionarCategoria(CatalogoSalsiao.TODAS);
    }

    private Button crearBotonCategoria(String categoria) {
        Button btn = new Button(emojiCategoria(categoria) + "  " + categoria);
        btn.getStyleClass().add("ventasCatBtn");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setUserData(categoria);
        btn.setOnAction(e -> seleccionarCategoria(categoria));
        return btn;
    }

    private String emojiCategoria(String cat) {
        return switch (cat) {
            case "Hamburguesas" -> "🍔";
            case "Hotdogs" -> "🌭";
            case "Pizzas" -> "🍕";
            case "Tacos" -> "🌮";
            case "Wraps" -> "🌯";
            case "Sándwiches" -> "🥪";
            case "Pollo / Alitas" -> "🍗";
            case "Picaderas" -> "🧀";
            case "Papas y extras" -> "🍟";
            case "Bebidas" -> "🥤";
            case "Postres" -> "🍰";
            case "Combos" -> "📦";
            default -> "⭐";
        };
    }

    private void seleccionarCategoria(String categoria) {
        categoriaActiva = categoria;

        if (lblCategoriaActiva != null) {
            lblCategoriaActiva.setText(CatalogoSalsiao.TODAS.equals(categoria)
                    ? "Todos los productos"
                    : categoria);
        }

        if (panelCategorias != null) {
            for (var node : panelCategorias.getChildren()) {
                if (node instanceof Button btn) {
                    boolean activo = categoria.equals(btn.getUserData());
                    btn.getStyleClass().remove("ventasCatBtnActive");
                    if (activo && !btn.getStyleClass().contains("ventasCatBtnActive")) {
                        btn.getStyleClass().add("ventasCatBtnActive");
                    }
                }
            }
        }

        renderizarProductos();
    }

    private void renderizarProductos() {
        if (gridProductos == null) return;

        gridProductos.getChildren().clear();
        String busqueda = txtBuscarMenu != null ? txtBuscarMenu.getText() : "";
        List<ProductoMenu> productos = CatalogoSalsiao.buscar(busqueda, categoriaActiva);

        if (productos.isEmpty()) {
            Label vacio = new Label("No hay productos en esta categoría");
            vacio.getStyleClass().add("ventasEmptyMsg");
            gridProductos.getChildren().add(vacio);
            return;
        }

        for (ProductoMenu producto : productos) {
            gridProductos.getChildren().add(
                    MenuDigitalUI.crearTarjetaProducto(producto, this::agregarAlCarrito)
            );
        }
    }

    private void agregarAlCarrito(ProductoMenu producto) {
        carrito.agregar(producto);
        actualizarCarritoUI();
    }

    private void actualizarCarritoUI() {
        if (panelItemsCarrito == null) return;

        panelItemsCarrito.getChildren().clear();

        if (carrito.estaVacio()) {
            Label vacio = new Label("Tu carrito está vacío\nAgrega productos del menú →");
            vacio.getStyleClass().add("ventasCarritoVacio");
            vacio.setWrapText(true);
            panelItemsCarrito.getChildren().add(vacio);
        } else {
            for (ItemCarrito item : carrito.getItems()) {
                panelItemsCarrito.getChildren().add(
                        MenuDigitalUI.crearFilaCarrito(
                                item,
                                () -> { item.incrementar(); actualizarCarritoUI(); },
                                () -> { item.decrementar(); actualizarCarritoUI(); },
                                () -> {
                                    carrito.eliminar(item);
                                    actualizarCarritoUI();
                                }
                        )
                );
            }
        }

        totalVentaActual = carrito.getSubtotal();
        String totalFmt = String.format("RD$ %,.2f", totalVentaActual);
        String itemsFmt = carrito.getCantidadTotalItems() + " producto"
                + (carrito.getCantidadTotalItems() == 1 ? "" : "s");

        if (lblContadorItems != null) lblContadorItems.setText(itemsFmt);
        if (lblTotalCarritoHeader != null) lblTotalCarritoHeader.setText(String.format("RD$ %,.0f", totalVentaActual));
        if (lblSubtotalCarrito != null) lblSubtotalCarrito.setText(totalFmt);
        if (lblTotalCarrito != null) lblTotalCarrito.setText(totalFmt);
        if (lblTotalPago != null) lblTotalPago.setText(totalFmt);

        calcularCambio();
    }

    @FXML
    private void vaciarCarrito() {
        carrito.vaciar();
        actualizarCarritoUI();
    }

    /**
     * Abre el modal POS: método de pago, devuelta, PDF y correo.
     */
    @FXML
    private void procesarPago() {
        if (carrito.estaVacio()) {
            Alertas.advertencia("Carrito vacío", "Agrega productos antes de procesar el pago.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/salsiaopf/pago-modal.fxml"));
            Parent root = loader.load();
            PagoModalController modalCtrl = loader.getController();

            modalCtrl.configurar(carrito, this::onVentaProcesadaExitosa);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            if (panelItemsCarrito != null && panelItemsCarrito.getScene() != null) {
                dialog.initOwner(panelItemsCarrito.getScene().getWindow());
            }

            Scene scene = new Scene(root);
            var css = getClass().getResource("/org/example/salsiaopf/styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            dialog.setScene(scene);
            dialog.setTitle("Procesar Pago - Salsiao");
            dialog.showAndWait();

        } catch (Exception e) {
            Alertas.error("Error", "No se pudo abrir el modal de pago: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Flujo rápido anterior: redirige al modal POS. */
    @FXML
    private void cobrarVenta() {
        procesarPago();
    }

    private void onVentaProcesadaExitosa(ResultadoProcesoVenta resultado) {
        var rutaPdf = resultado.getRutaPdf();

        // Abrir factura automáticamente en el visor predeterminado
        if (rutaPdf != null) {
            FacturaDesktopUtil.abrirPdf(rutaPdf);
        }

        StringBuilder msg = new StringBuilder();
        msg.append("Venta registrada correctamente.\n\n");
        msg.append("ID Venta: ").append(resultado.getIdVenta()).append("\n");
        msg.append("ID Factura: ").append(resultado.getIdFactura()).append("\n");
        msg.append(String.format("Total: RD$ %,.2f\n", totalVentaActual));
        if (rutaPdf != null) {
            msg.append("\nPDF: ").append(rutaPdf.toAbsolutePath());
        }
        if (resultado.getMensajeCorreo() != null && !resultado.getMensajeCorreo().isBlank()) {
            msg.append("\n\n").append(resultado.getMensajeCorreo());
        }

        ButtonType btnAbrir = new ButtonType("Abrir PDF");
        ButtonType btnImprimir = new ButtonType("Imprimir");
        ButtonType btnOk = ButtonType.OK;

        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg.toString(), btnAbrir, btnImprimir, btnOk);
        alert.setTitle("Venta exitosa");
        alert.setHeaderText("✅ Cobro completado — Salsiao");
        alert.getDialogPane().setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

        Optional<ButtonType> opcion = alert.showAndWait();
        if (rutaPdf != null && opcion.isPresent()) {
            if (opcion.get() == btnAbrir) {
                if (!FacturaDesktopUtil.abrirPdf(rutaPdf)) {
                    Alertas.advertencia("PDF", "No se pudo abrir el archivo. Ruta:\n" + rutaPdf);
                }
            } else if (opcion.get() == btnImprimir) {
                if (!FacturaDesktopUtil.imprimirPdf(rutaPdf)) {
                    Alertas.advertencia("Impresión", "No se pudo enviar a impresora. Abra el PDF manualmente.");
                }
            }
        }

        // Carrito vaciado en PagoService tras confirmación exitosa
        actualizarCarritoUI();
    }

    @FXML
    private void irAPago() {
        if (carrito.estaVacio()) {
            Alertas.advertencia("Sin productos", "Agrega productos al carrito antes de ir a pago.");
            return;
        }
        actualizarCarritoUI();
        mostrarPago();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Lógica existente del módulo
    // ═══════════════════════════════════════════════════════════════════

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null && logoImage != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setFitWidth(82);
                logoImage.setFitHeight(82);
                logoImage.setPreserveRatio(true);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle();
                clip.setRadius(41);
                clip.setCenterX(41);
                clip.setCenterY(41);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void calcularCambio() {
        if (lblCambio == null || txtMontoRecibido == null) return;
        try {
            double recibido = Double.parseDouble(
                    txtMontoRecibido.getText().isEmpty() ? "0" : txtMontoRecibido.getText()
            );
            double cambio = recibido - totalVentaActual;
            lblCambio.setText("RD$ " + String.format("%,.2f", cambio));
        } catch (Exception e) {
            lblCambio.setText("RD$ 0.00");
        }
    }

    private void iniciarReloj() {
        if (lblFechaActual == null || lblHoraActual == null) return;
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("hh:mm a");

        Timeline reloj = new Timeline(
                new KeyFrame(Duration.seconds(0), event -> {
                    lblFechaActual.setText(LocalDate.now().format(formatoFecha));
                    lblHoraActual.setText(LocalTime.now().format(formatoHora));
                }),
                new KeyFrame(Duration.seconds(1))
        );
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo.");
    }

    private void ocultarTodas() {
        ocultar(viewProductos);
        ocultar(viewControlVenta);
        ocultar(viewOrdenActual);
        ocultar(viewPago);
        ocultar(viewFacturacion);
        ocultar(viewPedidosPendientes);
        ocultar(viewDelivery);
        ocultar(viewMesas);
        ocultar(viewHistorialVentas);
        ocultar(viewCaja);
    }

    private void ocultar(VBox vista) {
        if (vista != null) {
            vista.setVisible(false);
            vista.setManaged(false);
        }
    }

    private void mostrar(VBox vista) {
        if (vista != null) {
            vista.setVisible(true);
            vista.setManaged(true);
        }
    }

    @FXML private void mostrarProductos() { ocultarTodas(); mostrar(viewProductos); }
    @FXML private void mostrarControlVenta() { ocultarTodas(); mostrar(viewControlVenta); renderizarProductos(); }
    @FXML private void mostrarOrdenActual() { ocultarTodas(); mostrar(viewOrdenActual); }
    @FXML private void mostrarPago() { ocultarTodas(); mostrar(viewPago); actualizarCarritoUI(); }
    @FXML private void mostrarFacturacion() { ocultarTodas(); mostrar(viewFacturacion); }
    @FXML private void mostrarPedidosPendientes() { ocultarTodas(); mostrar(viewPedidosPendientes); }
    @FXML private void mostrarDelivery() { ocultarTodas(); mostrar(viewDelivery); }
    @FXML private void mostrarMesas() { ocultarTodas(); mostrar(viewMesas); }
    @FXML private void mostrarHistorialVentas() { ocultarTodas(); mostrar(viewHistorialVentas); }
    @FXML private void mostrarCaja() { ocultarTodas(); mostrar(viewCaja); }

    @FXML
    private void actualizarPagoView() {
        actualizarCarritoUI();
        Alertas.informacion("Vista actualizada", "La vista de pago se ha refrescado con los datos actuales del carrito.");
    }

    @FXML
    private void cancelarPagoView() {
        if (!carrito.estaVacio()) {
            var confirmar = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION,
                    "¿Seguro que deseas cancelar? El carrito se mantendrá intacto.",
                    javafx.scene.control.ButtonType.YES,
                    javafx.scene.control.ButtonType.NO);
            confirmar.setTitle("Cancelar pago");
            confirmar.setHeaderText("Cancelar operación de pago");
            var resultado = confirmar.showAndWait();
            if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.YES) {
                mostrarControlVenta();
            }
        } else {
            mostrarControlVenta();
        }
    }

    @FXML
    private void facturarDesdeViewPago() {
        if (carrito.estaVacio()) {
            Alertas.advertencia("Sin productos", "Agrega productos al carrito antes de facturar.");
            return;
        }
        procesarPago();
    }

    @FXML
    private void imprimirTicket() {
        if (carrito.estaVacio()) {
            Alertas.advertencia("Sin productos", "No hay productos para imprimir.");
            return;
        }
        Alertas.informacion("Imprimir ticket", "Función de impresión de ticket en desarrollo. Usa \"Procesar Pago\" para generar la factura PDF.");
    }

    @FXML
    private void cancelarVenta() {
        if (carrito.estaVacio()) {
            Alertas.advertencia("Carrito vacío", "No hay venta que cancelar.");
            return;
        }
        var confirmar = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "¿Seguro que deseas cancelar esta venta? Se vaciará el carrito.",
                javafx.scene.control.ButtonType.YES,
                javafx.scene.control.ButtonType.NO);
        confirmar.setTitle("Cancelar venta");
        confirmar.setHeaderText("Cancelar venta actual");
        var resultado = confirmar.showAndWait();
        if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.YES) {
            carrito.vaciar();
            actualizarCarritoUI();
            mostrarControlVenta();
        }
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        SessionManager.getInstance().cerrarSesion();
        System.exit(0);
    }
}
