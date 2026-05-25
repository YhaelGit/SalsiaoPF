package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.salsiaopf.dao.CajaDAO;
import org.example.salsiaopf.dao.ClienteDAO;
import org.example.salsiaopf.dao.DetalleVentaDAO;
import org.example.salsiaopf.dao.PedidoVentaDAO;
import org.example.salsiaopf.dao.VentaDAO;
import org.example.salsiaopf.database.ConexionBD;
import org.example.salsiaopf.model.Caja;
import org.example.salsiaopf.model.Cliente;
import org.example.salsiaopf.model.PedidoVenta;
import org.example.salsiaopf.service.FacturaService;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;
import org.example.salsiaopf.ventas.*;

import java.awt.Desktop;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VentaController {

    @FXML private Button btnDashboard, btnNuevaVenta, btnHistorial, btnLimpiar;
    @FXML private StackPane contentPanel;
    @FXML private ScrollPane scrollDashboard, scrollVenta, scrollHistorial;

    @FXML private Label lblVentasHoy, lblCantVentasHoy, lblTotalProductos, lblTipoVentaFrecuente;
    @FXML private TableView<Map<String, Object>> tablaVentasRecientes;
    @FXML private TableColumn<Map<String, Object>, String> colFactura, colFecha, colCliente, colTipo, colMetodo, colTotal;

    @FXML private TextField txtBuscar;
    @FXML private FlowPane panelCategorias, panelProductos;

    @FXML private ListView<ItemCarrito> listCart;
    @FXML private Label lblCartCount, lblSubtotal, lblITBIS, lblTotal;
    @FXML private TextField txtDescuento, txtDelivery;
    @FXML private ComboBox<String> cmbTipoVenta;
    @FXML private ComboBox<org.example.salsiaopf.model.Cliente> cmbCliente;
    @FXML private Button btnCheckout;

    @FXML private StackPane pagoOverlay;
    @FXML private Label lblPagoTotal, lblDevuelta;
    @FXML private Label lblPagoClienteNombre, lblPagoClienteTelefono, lblPagoClienteEmail, lblPagoClienteDireccion;
    @FXML private VBox panelClienteInfo;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private ComboBox<Cliente> cmbPagoCliente;
    @FXML private TextField txtMontoRecibido, txtEmailCliente, txtObservaciones;
    @FXML private Button btnConfirmarPago;

    @FXML private TableView<Map<String, Object>> tablaHistorial;
    @FXML private TableColumn<Map<String, Object>, String> colHFactura, colHFecha, colHCliente, colHTipo, colHMetodo, colHTotal;
    @FXML private DatePicker dpDesde, dpHasta;

    @FXML private Button btnCaja, btnPedidos;
    @FXML private ScrollPane scrollCaja, scrollPedidos;

    @FXML private Label lblEstadoCaja, lblCajaUsuario, lblCajaApertura, lblCajaMontoInicial;
    @FXML private Label lblCajaEfectivo, lblCajaTarjeta, lblCajaTransferencia, lblCajaTotalGeneral;
    @FXML private Label lblCajaEfectivoEsp, lblCajaTarjetaEsp, lblCajaTransferenciaEsp, lblCajaTotalGeneralEsp;
    @FXML private Label lblCajaEfectivoDiff, lblCajaTarjetaDiff, lblCajaTransferenciaDiff;
    @FXML private Label lblCajaDiffForm;
    @FXML private TextField txtCajaMontoInicial, txtCajaEfectivo, txtCajaTarjeta, txtCajaTransferencia;
    @FXML private Label lblCajaDiferencia;
    @FXML private Button btnAbrirCaja, btnCerrarCaja;
    @FXML private TableView<org.example.salsiaopf.model.Caja> tablaCierres;
    @FXML private TableColumn<org.example.salsiaopf.model.Caja, String> colCFechaApertura, colCFechaCierre, colCUsuario;
    @FXML private TableColumn<org.example.salsiaopf.model.Caja, String> colCInicial, colCEfectivo, colCTarjeta, colCTransferencia, colCTotal, colCDiferencia;

    @FXML private ComboBox<Cliente> cmbFiltroCliente;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TextField txtBuscarPedido;
    @FXML private TableView<org.example.salsiaopf.model.PedidoVenta> tablaPedidos;
    @FXML private TableColumn<org.example.salsiaopf.model.PedidoVenta, String> colPId, colPFactura, colPCliente, colPItems, colPTotal, colPEstado, colPFecha, colPTipo;
    @FXML private VBox panelDetallePedido;
    @FXML private Label lblDetalleFactura, lblDetalleCliente, lblDetalleItems, lblDetalleFecha;
    @FXML private Label lblDetalleTotal, lblDetalleTipo, lblDetallePago, lblDetalleObservaciones;
    @FXML private Button btnCambiarEstado, btnCobrarPedido, btnCancelarPedido;
    @FXML private ComboBox<Cliente> cmbPedidoCliente;

    private final CarritoVentas carrito = new CarritoVentas();
    private String categoriaActual = CatalogoSalsiao.TODAS;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("es", "DO"));
    private String viewActual = "dashboard";

    @FXML
    public void initialize() {
        fmt.setMaximumFractionDigits(2);
        fmt.setMinimumFractionDigits(2);

        VentaDAO.crearTablasSiNoExisten();
        DetalleVentaDAO.crearTablasSiNoExisten();
        CajaDAO.crearTablasSiNoExisten();
        PedidoVentaDAO.crearTablasSiNoExisten();

        cmbTipoVenta.getItems().setAll("Mostrador", "Delivery", "Mesa", "WhatsApp");
        cmbTipoVenta.setValue("Mostrador");

        try {
            List<Cliente> clientes = ClienteDAO.listarClientes();
            cmbCliente.getItems().addAll(clientes);
            cmbPagoCliente.getItems().addAll(clientes);

            cmbCliente.setOnAction(e -> {
                Cliente c = cmbCliente.getValue();
                if (c != null) {
                    cmbPagoCliente.setValue(c);
                    txtEmailCliente.setText(c.getEmail() != null ? c.getEmail() : "");
                }
            });

            cmbPagoCliente.setOnAction(e -> {
                Cliente c = cmbPagoCliente.getValue();
                if (c != null) {
                    cmbCliente.setValue(c);
                    txtEmailCliente.setText(c.getEmail() != null ? c.getEmail() : "");
                    lblPagoClienteNombre.setText("👤 " + c.getNombre() + " " + (c.getApellido() != null ? c.getApellido() : ""));
                    lblPagoClienteTelefono.setText("📞 " + (c.getTelefono() != null ? c.getTelefono() : "—"));
                    lblPagoClienteEmail.setText("✉ " + (c.getEmail() != null ? c.getEmail() : "—"));
                    lblPagoClienteDireccion.setText("📍 " + (c.getDireccion() != null ? c.getDireccion() : "—"));
                    panelClienteInfo.setVisible(true);
                    panelClienteInfo.setManaged(true);
                } else {
                    panelClienteInfo.setVisible(false);
                    panelClienteInfo.setManaged(false);
                }
            });
        } catch (Exception e) {
            System.out.println("[Venta] Error cargando clientes: " + e.getMessage());
        }
        cmbMetodoPago.getItems().setAll("Efectivo", "Tarjeta", "Transferencia");
        cmbMetodoPago.setValue("Efectivo");

        txtMontoRecibido.textProperty().addListener((o, a, b) -> calcularDevuelta());
        txtDescuento.textProperty().addListener((o, a, b) -> actualizarTotales());
        txtDelivery.textProperty().addListener((o, a, b) -> actualizarTotales());

        carrito.tipoVentaProperty().bind(cmbTipoVenta.valueProperty());

        configurarColumnas();
        configurarColumnasCaja();
        configurarColumnasPedidos();
        setupCartList();
        setupCategories();
        cargarProductos();
        txtBuscar.textProperty().addListener((o, a, b) -> cargarProductos());

        cmbFiltroEstado.getItems().setAll("Todos", "Pendiente", "Preparando", "Listo", "En camino", "Entregado", "Cancelado");
        cmbFiltroEstado.setValue("Todos");
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((o, a, p) -> mostrarDetallePedido(p));

        try {
            List<Cliente> todosClientes = ClienteDAO.listarClientes();
            cmbFiltroCliente.getItems().addAll(todosClientes);
            cmbPedidoCliente.getItems().addAll(todosClientes);
            cmbFiltroCliente.setOnAction(e -> cargarPedidos());
        } catch (Exception e) {
            System.out.println("[Venta] Error cargando clientes: " + e.getMessage());
        }

        txtCajaEfectivo.textProperty().addListener((o, a, b) -> calcCajaTotales());
        txtCajaTarjeta.textProperty().addListener((o, a, b) -> calcCajaTotales());
        txtCajaTransferencia.textProperty().addListener((o, a, b) -> calcCajaTotales());

        cargarEstadoCaja();
        cargarCierres();
        cargarPedidos();

        mostrarDashboard();
    }

    private void configurarColumnas() {
        configurarColumna(colFactura, "id_factura");
        configurarColumna(colFecha, "fecha_venta");
        configurarColumna(colCliente, "cliente_nombre");
        configurarColumna(colTipo, "tipo_venta");
        configurarColumna(colMetodo, "metodo_pago");
        configurarColumnaTotal(colTotal, "total");

        configurarColumna(colHFactura, "id_factura");
        configurarColumna(colHFecha, "fecha_venta");
        configurarColumna(colHCliente, "cliente_nombre");
        configurarColumna(colHTipo, "tipo_venta");
        configurarColumna(colHMetodo, "metodo_pago");
        configurarColumnaTotal(colHTotal, "total");
    }

    private void configurarColumna(TableColumn<Map<String, Object>, String> col, String campo) {
        col.setCellValueFactory(cd -> {
            Object val = cd.getValue().get(campo);
            return new SimpleStringProperty(val == null ? "" : val.toString());
        });
    }

    private void configurarColumnaTotal(TableColumn<Map<String, Object>, String> col, String campo) {
        col.setCellValueFactory(cd -> {
            Object val = cd.getValue().get(campo);
            if (val instanceof Number n) return new SimpleStringProperty(fmt.format(n.doubleValue()));
            return new SimpleStringProperty(val == null ? "RD$ 0.00" : val.toString());
        });
    }

    private void setupCartList() {
        listCart.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ItemCarrito item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    box.getStyleClass().add("posCartItem");

                    Text emoji = new Text(item.getProducto().getEmoji());
                    emoji.getStyleClass().add("posCartItemEmoji");

                    VBox info = new VBox(2);
                    Text nombre = new Text(item.getProducto().getNombre());
                    nombre.getStyleClass().add("posCartItemName");
                    String extrasStr = item.getExtras().isEmpty() ? "" : " + " + String.join(", +", item.getExtras());
                    String notaStr = item.getNota().isBlank() ? "" : " | " + item.getNota();
                    Text detalles = new Text(fmt.format(item.getPrecioUnitario()) + " c/u" + extrasStr + notaStr);
                    detalles.getStyleClass().add("posCartItemDetail");
                    info.getChildren().addAll(nombre, detalles);
                    HBox.setHgrow(info, Priority.ALWAYS);

                    Button btnMinus = new Button("−");
                    btnMinus.getStyleClass().add("posQtyBtn");
                    Text qty = new Text(String.valueOf(item.getCantidad()));
                    qty.getStyleClass().add("posQtyValue");
                    Button btnPlus = new Button("+");
                    btnPlus.getStyleClass().add("posQtyBtn");

                    Text subtotal = new Text(fmt.format(item.getSubtotal()));
                    subtotal.getStyleClass().add("posCartItemSubtotal");

                    Button btnEdit = new Button("✎");
                    btnEdit.getStyleClass().add("posEditBtn");

                    Button btnRemove = new Button("✕");
                    btnRemove.getStyleClass().add("posRemoveBtn");

                    btnMinus.setOnAction(e -> { item.decrementar(); refreshCart(); });
                    btnPlus.setOnAction(e -> { item.incrementar(); refreshCart(); });
                    btnEdit.setOnAction(e -> mostrarDialogoEditarItem(item));
                    btnRemove.setOnAction(e -> { carrito.eliminar(item); refreshCart(); });

                    HBox qtyBox = new HBox(4, btnMinus, qty, btnPlus);
                    qtyBox.setAlignment(Pos.CENTER);

                    box.getChildren().addAll(emoji, info, qtyBox, subtotal, btnEdit, btnRemove);
                    setGraphic(box);
                    setStyle("-fx-background-color: transparent; -fx-padding: 2 0;");
                }
            }
        });
    }

    private void refreshCart() {
        listCart.setItems(null);
        listCart.setItems(carrito.getItems());
        lblCartCount.setText(String.valueOf(carrito.getCantidadTotalItems()));
        actualizarTotales();
    }

    private void actualizarTotales() {
        double desc = parseDouble(txtDescuento.getText());
        double del = parseDouble(txtDelivery.getText());
        carrito.setDescuento(desc);
        carrito.setDelivery(del);
        lblSubtotal.setText(fmt.format(carrito.getSubtotal()));
        lblITBIS.setText(fmt.format(carrito.getITBIS()));
        lblTotal.setText(fmt.format(carrito.getTotal()));
        btnCheckout.setDisable(carrito.estaVacio());
    }

    private void mostrarView(String view) {
        viewActual = view;
        scrollDashboard.setVisible(false); scrollDashboard.setManaged(false);
        scrollVenta.setVisible(false); scrollVenta.setManaged(false);
        scrollHistorial.setVisible(false); scrollHistorial.setManaged(false);
        scrollCaja.setVisible(false); scrollCaja.setManaged(false);
        scrollPedidos.setVisible(false); scrollPedidos.setManaged(false);
        pagoOverlay.setVisible(false); pagoOverlay.setManaged(false);

        ScrollPane target = null;
        btnDashboard.getStyleClass().setAll("sideButton");
        btnNuevaVenta.getStyleClass().setAll("sideButton");
        btnHistorial.getStyleClass().setAll("sideButton");
        btnCaja.getStyleClass().setAll("sideButton");
        btnPedidos.getStyleClass().setAll("sideButton");

        switch (view) {
            case "venta" -> {
                btnNuevaVenta.getStyleClass().setAll("sideButtonActive");
                target = scrollVenta;
            }
            case "historial" -> {
                btnHistorial.getStyleClass().setAll("sideButtonActive");
                target = scrollHistorial;
            }
            case "caja" -> {
                btnCaja.getStyleClass().setAll("sideButtonActive");
                target = scrollCaja;
            }
            case "pedidos" -> {
                btnPedidos.getStyleClass().setAll("sideButtonActive");
                target = scrollPedidos;
            }
            default -> btnDashboard.getStyleClass().setAll("sideButtonActive");
        }
        if (target == null) target = scrollDashboard;
        target.setVisible(true);
        target.setManaged(true);
    }

    @FXML void mostrarDashboard() { mostrarView("dashboard"); cargarDashboard(); }
    @FXML void mostrarVenta() { mostrarView("venta"); }
    @FXML void mostrarHistorial() { mostrarView("historial"); cargarHistorial(); }
    @FXML void mostrarCaja() { mostrarView("caja"); cargarEstadoCaja(); cargarCierres(); }
    @FXML void mostrarPedidos() { mostrarView("pedidos"); cargarPedidos(); }

    private void cargarDashboard() {
        double totalHoy = VentaDAO.obtenerTotalVentasHoy();
        int cantHoy = VentaDAO.obtenerCantidadVentasHoy();
        lblVentasHoy.setText(fmt.format(totalHoy));
        lblCantVentasHoy.setText(cantHoy + " transacciones");
        lblTotalProductos.setText(String.valueOf(CatalogoSalsiao.todos().size()));
        lblTipoVentaFrecuente.setText("Mostrador");
        tablaVentasRecientes.setItems(FXCollections.observableArrayList(VentaDAO.obtenerVentasRecientes(50)));
    }

    private void cargarHistorial() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde != null && hasta != null) {
            if (hasta.isBefore(desde)) { Alertas.error("Fechas inválidas", "La fecha 'Hasta' debe ser mayor o igual a 'Desde'."); return; }
            tablaHistorial.setItems(FXCollections.observableArrayList(VentaDAO.obtenerVentasPorRango(desde, hasta)));
        } else {
            tablaHistorial.setItems(FXCollections.observableArrayList(VentaDAO.obtenerVentasRecientes(500)));
        }
    }

    @FXML void filtrarHistorial() {
        if (dpDesde.getValue() == null || dpHasta.getValue() == null) {
            Alertas.error("Fechas requeridas", "Seleccione ambas fechas (Desde y Hasta) para filtrar.");
            return;
        }
        cargarHistorial();
    }
    @FXML void limpiarFiltrosHistorial() { dpDesde.setValue(null); dpHasta.setValue(null); cargarHistorial(); }

    @FXML void volverMenu(ActionEvent e) { Navegacion.volverCentroSistema(e); }

    @FXML void limpiar() {
        switch (viewActual) {
            case "venta" -> {
                carrito.vaciar(); txtBuscar.clear();
                txtDescuento.setText("0"); txtDelivery.setText("0");
                cmbTipoVenta.setValue("Mostrador");
                cmbCliente.setValue(null); cmbPagoCliente.setValue(null);
                txtObservaciones.clear();
                panelClienteInfo.setVisible(false); panelClienteInfo.setManaged(false);
                refreshCart(); cargarProductos();
            }
            case "dashboard" -> cargarDashboard();
            case "historial" -> cargarHistorial();
            case "caja" -> { cargarEstadoCaja(); cargarCierres(); }
            case "pedidos" -> { cmbFiltroCliente.setValue(null); cmbFiltroEstado.setValue("Todos"); txtBuscarPedido.clear(); cargarPedidos(); ocultarDetallePedido(); }
        }
    }

    private void setupCategories() {
        ToggleGroup group = new ToggleGroup();
        for (String cat : CatalogoSalsiao.CATEGORIAS) {
            ToggleButton btn = new ToggleButton(cat);
            btn.setToggleGroup(group);
            btn.getStyleClass().add("posCategoryBtn");
            btn.setOnAction(e -> { categoriaActual = cat; cargarProductos(); });
            panelCategorias.getChildren().add(btn);
        }
        ((ToggleButton) panelCategorias.getChildren().get(0)).setSelected(true);
    }

    private void cargarProductos() {
        panelProductos.getChildren().clear();
        for (ProductoMenu p : CatalogoSalsiao.buscar(txtBuscar.getText(), categoriaActual)) {
            panelProductos.getChildren().add(crearCardProducto(p));
        }
    }

    private VBox crearCardProducto(ProductoMenu p) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12, 10, 10, 10));
        card.setPrefWidth(148);
        card.setMaxWidth(148);
        card.setMinHeight(164);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("posProductCard");
        String[] grad = CatalogoSalsiao.gradienteCategoria(p.getCategoria());
        card.setStyle(String.format("-fx-background-color: linear-gradient(to bottom right, %s, %s); -fx-background-radius: 16;", grad[0], grad[1]));

        Text emoji = new Text(p.getEmoji());
        emoji.getStyleClass().add("posProductEmoji");

        Text name = new Text(p.getNombre());
        name.getStyleClass().add("posProductName");
        name.setWrappingWidth(128);

        Text price = new Text(p.getPrecioFormateado());
        price.getStyleClass().add("posProductPrice");

        Text desc = new Text(p.getDescripcion());
        desc.getStyleClass().add("posProductDesc");
        desc.setWrappingWidth(128);

        card.getChildren().addAll(emoji, name, price, desc);
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ItemCarrito temp = new ItemCarrito(p, 1);
                mostrarDialogoEditarItem(temp);
                if (!temp.getExtras().isEmpty() || (temp.getNota() != null && !temp.getNota().isBlank())) {
                    carrito.getItems().add(temp);
                    refreshCart();
                } else {
                    carrito.agregar(p);
                    refreshCart();
                }
            } else {
                carrito.agregar(p);
                refreshCart();
            }
        });
        return card;
    }

    private void mostrarDialogoEditarItem(ItemCarrito item) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Personalizar: " + item.getProducto().getNombre());
        dialog.setHeaderText("Agregue extras y notas");

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        Label lblExtras = new Label("Extras (RD$ 50 c/u):");
        lblExtras.setStyle("-fx-font-weight: bold;");
        String[] opcionesExtras = {"Queso extra", "Bacon", "Jalapeño", "Cheddar", "Salsa BBQ", "Salsa Especial", "Lechuga", "Tomate", "Cebolla"};
        FlowPane extrasPane = new FlowPane(8, 8);
        List<CheckBox> checks = new java.util.ArrayList<>();
        for (String extra : opcionesExtras) {
            CheckBox cb = new CheckBox(extra);
            if (item.getExtras().contains(extra)) cb.setSelected(true);
            checks.add(cb);
            extrasPane.getChildren().add(cb);
        }

        Label lblNota = new Label("Nota:");
        lblNota.setStyle("-fx-font-weight: bold;");
        TextField txtNota = new TextField(item.getNota());
        txtNota.setPromptText("Ej: Sin cebolla, bien tostado...");

        content.getChildren().addAll(lblExtras, extrasPane, lblNota, txtNota);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                item.getExtras().clear();
                for (CheckBox cb : checks) {
                    if (cb.isSelected()) item.getExtras().add(cb.getText());
                }
                item.setNota(txtNota.getText());
                refreshCart();
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML void procesarPago() {
        if (carrito.estaVacio()) { Alertas.error("Carrito vacío", "Agregue productos primero."); return; }
        lblPagoTotal.setText(fmt.format(carrito.getTotal()));
        txtMontoRecibido.setText(String.valueOf((int) Math.ceil(carrito.getTotal())));
        txtObservaciones.clear();

        Cliente c = cmbCliente.getValue();
        if (c != null) {
            cmbPagoCliente.setValue(c);
            txtEmailCliente.setText(c.getEmail() != null ? c.getEmail() : "");
            lblPagoClienteNombre.setText("👤 " + c.getNombre() + " " + (c.getApellido() != null ? c.getApellido() : ""));
            lblPagoClienteTelefono.setText("📞 " + (c.getTelefono() != null ? c.getTelefono() : "—"));
            lblPagoClienteEmail.setText("✉ " + (c.getEmail() != null ? c.getEmail() : "—"));
            lblPagoClienteDireccion.setText("📍 " + (c.getDireccion() != null ? c.getDireccion() : "—"));
            panelClienteInfo.setVisible(true);
            panelClienteInfo.setManaged(true);
        } else {
            txtEmailCliente.clear();
            panelClienteInfo.setVisible(false);
            panelClienteInfo.setManaged(false);
        }

        calcularDevuelta();
        pagoOverlay.setVisible(true); pagoOverlay.setManaged(true);
    }

    private void calcularDevuelta() {
        double dev = Math.max(0, parseDouble(txtMontoRecibido.getText()) - carrito.getTotal());
        lblDevuelta.setText(fmt.format(dev));
    }

    @FXML void cancelarPago() { pagoOverlay.setVisible(false); pagoOverlay.setManaged(false); }

    @FXML void confirmarPago() {
        btnConfirmarPago.setDisable(true);
        String email = txtEmailCliente.getText().trim();
        String metodo = cmbMetodoPago.getValue();
        double recibido = parseDouble(txtMontoRecibido.getText());
        double devuelta = Math.max(0, recibido - carrito.getTotal());

        if (metodo.equals("Efectivo") && recibido < carrito.getTotal()) {
            Alertas.error("Monto insuficiente", "El monto recibido debe ser mayor o igual al total.");
            btnConfirmarPago.setDisable(false); return;
        }

        int idCliente = 1;
        Cliente clienteSel = cmbPagoCliente.getValue();
        if (clienteSel == null) clienteSel = cmbCliente.getValue();
        if (clienteSel != null) idCliente = clienteSel.getIdCliente();

        String idFactura = VentaDAO.generarIdFactura();
        Connection conn = null;
        Path rutaPdf = null;
        try {
            conn = ConexionBD.conectar();
            if (conn == null) throw new SQLException("Sin conexión a BD");
            conn.setAutoCommit(false);

            int idVenta = VentaDAO.insertarVenta(conn, idFactura, cmbTipoVenta.getValue(),
                carrito.getSubtotal(), carrito.getITBIS(), carrito.getDescuento(),
                carrito.getDelivery(), carrito.getTotal(), metodo, recibido, devuelta,
                email, idCliente);

            DetalleVentaDAO.insertarDetalles(conn, idVenta, carrito.copiarItems());
            conn.commit();
            VentaDAO.guardarVenta(idCliente, carrito.getTotal());

            try {
                String cliName = clienteSel != null ? clienteSel.getNombre() + " " + (clienteSel.getApellido() != null ? clienteSel.getApellido() : "") : "";
                String cliPhone = clienteSel != null ? (clienteSel.getTelefono() != null ? clienteSel.getTelefono() : "") : "";
                String cliAddr = clienteSel != null ? (clienteSel.getDireccion() != null ? clienteSel.getDireccion() : "") : "";
                rutaPdf = FacturaService.generarFacturaDirecta(carrito.copiarItems(),
                    cliName, cliPhone, cliAddr, email, metodo,
                    java.math.BigDecimal.valueOf(carrito.getDescuento()),
                    java.math.BigDecimal.valueOf(carrito.getDelivery()),
                    java.math.BigDecimal.valueOf(recibido),
                    java.math.BigDecimal.valueOf(devuelta));
            } catch (Exception jrErr) {
                System.out.println("[Venta] PDF no generado: " + jrErr.getMessage());
            }

            String tv = cmbTipoVenta.getValue();
            PedidoVenta pedido = new PedidoVenta();
            pedido.setIdFactura(idFactura);
            String cliFullName = (clienteSel != null) ? clienteSel.getNombre() + " " + (clienteSel.getApellido() != null ? clienteSel.getApellido() : "") : "Consumidor Final";
            pedido.setClienteNombre(cliFullName.trim());
            StringBuilder itemsText = new StringBuilder();
            var itemsList = carrito.copiarItems();
            for (int i = 0; i < itemsList.size(); i++) {
                if (i > 0) itemsText.append(", ");
                itemsText.append(itemsList.get(i).getProducto().getNombre()).append(" x").append(itemsList.get(i).getCantidad());
                if (!itemsList.get(i).getExtras().isEmpty()) {
                    itemsText.append(" [").append(String.join(", ", itemsList.get(i).getExtras())).append("]");
                }
            }
            pedido.setItemsTexto(itemsText.toString());
            pedido.setTotal(carrito.getTotal());
            pedido.setEstado("Pendiente");
            pedido.setTipoVenta(tv);
            pedido.setMetodoPago(metodo);
            pedido.setObservaciones(txtObservaciones.getText());
            PedidoVentaDAO.insertar(pedido);

            String msg = "Factura: " + idFactura + "\nTotal: " + fmt.format(carrito.getTotal())
                + "\nMétodo: " + metodo + "\nDevuelta: " + fmt.format(devuelta);

            Path pdfFinal = rutaPdf;
            if (pdfFinal != null) msg += "\n\n📄 PDF: " + pdfFinal.getFileName();
            Alertas.exito("Venta Completada", msg);

            if (pdfFinal != null && Desktop.isDesktopSupported()) {
                try { Desktop.getDesktop().open(pdfFinal.toFile()); }
                catch (Exception e) { System.out.println("[Venta] No se pudo abrir PDF: " + e.getMessage()); }
            }
            carrito.vaciar(); refreshCart(); cancelarPago(); mostrarDashboard();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (Exception ignored) {} }
            Alertas.error("Error", "Error al guardar venta: " + e.getMessage());
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {} }
            btnConfirmarPago.setDisable(false);
        }
    }

    // ── CAJA ──────────────────────────────────────────────────────────────────

    private void configurarColumnasCaja() {
        colCFechaApertura.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFechaApertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colCFechaCierre.setCellValueFactory(cd -> {
            if (cd.getValue().getFechaCierre() == null) return new SimpleStringProperty("--");
            return new SimpleStringProperty(cd.getValue().getFechaCierre().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });
        colCUsuario.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUsuario()));
        colCInicial.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getMontoInicial())));
        colCEfectivo.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getTotalEfectivo())));
        colCTarjeta.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getTotalTarjeta())));
        colCTransferencia.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getTotalTransferencia())));
        colCTotal.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getTotalGeneral())));
        colCDiferencia.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getDiferencia())));
    }

    private void cargarEstadoCaja() {
        Map<String, Double> esperado = VentaDAO.obtenerTotalesHoyPorMetodo();
        double efEsperado = esperado.getOrDefault("Efectivo", 0.0);
        double tarEsperado = esperado.getOrDefault("Tarjeta", 0.0);
        double transEsperado = esperado.getOrDefault("Transferencia", 0.0);

        if (CajaDAO.hayCajaAbierta()) {
            Caja c = CajaDAO.obtenerCajaAbierta();
            if (c != null) {
                lblEstadoCaja.setText("🟢 Abierta");
                lblEstadoCaja.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#059669;");
                lblCajaUsuario.setText("Usuario: " + c.getUsuario());
                lblCajaApertura.setText(c.getFechaApertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                lblCajaMontoInicial.setText(fmt.format(c.getMontoInicial()));
                btnAbrirCaja.setDisable(true);
                btnCerrarCaja.setDisable(false);
                txtCajaMontoInicial.setText(String.valueOf((int) c.getMontoInicial()));

                txtCajaEfectivo.setText(String.valueOf((int) efEsperado));
                txtCajaTarjeta.setText(String.valueOf((int) tarEsperado));
                txtCajaTransferencia.setText(String.valueOf((int) transEsperado));
                return;
            }
        }
        lblEstadoCaja.setText("🔴 Cerrada - Ventas hoy: " + fmt.format(efEsperado + tarEsperado + transEsperado));
        lblEstadoCaja.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-text-fill:#dc2626;");
        lblCajaUsuario.setText("");
        lblCajaApertura.setText("--:--");
        lblCajaMontoInicial.setText("RD$ 0.00");
        btnAbrirCaja.setDisable(false);
        btnCerrarCaja.setDisable(true);
        txtCajaEfectivo.setText(String.valueOf((int) efEsperado));
        txtCajaTarjeta.setText(String.valueOf((int) tarEsperado));
        txtCajaTransferencia.setText(String.valueOf((int) transEsperado));
    }

    @FXML void abrirCaja() {
        if (CajaDAO.hayCajaAbierta()) { Alertas.error("Caja ya abierta", "Ya hay una caja abierta. Ciérrela primero."); return; }
        double monto = parseDouble(txtCajaMontoInicial.getText());
        String usuario = SessionManager.getInstance().getUsuarioActivo().getNombre();
        int id = CajaDAO.abrirCaja(monto, usuario);
        if (id > 0) {
            Alertas.exito("Caja abierta", "Caja aperturada con RD$ " + fmt.format(monto) + "\nUsuario: " + usuario);
            cargarEstadoCaja();
        } else {
            Alertas.error("Error", "No se pudo abrir la caja.");
        }
    }

    @FXML void cerrarCaja() {
        if (!CajaDAO.hayCajaAbierta()) return;
        Caja c = CajaDAO.obtenerCajaAbierta();
        if (c == null) return;
        double efDeclarado = parseDouble(txtCajaEfectivo.getText());
        double tarDeclarado = parseDouble(txtCajaTarjeta.getText());
        double transDeclarado = parseDouble(txtCajaTransferencia.getText());

        Map<String, Double> esperado = VentaDAO.obtenerTotalesHoyPorMetodo();
        double efEsperado = esperado.getOrDefault("Efectivo", 0.0);
        double tarEsperado = esperado.getOrDefault("Tarjeta", 0.0);
        double transEsperado = esperado.getOrDefault("Transferencia", 0.0);

        double totalDeclarado = efDeclarado + tarDeclarado + transDeclarado;
        double totalEsperado = efEsperado + tarEsperado + transEsperado;
        double diffEfectivo = efDeclarado - efEsperado;
        double diffTarjeta = tarDeclarado - tarEsperado;
        double diffTrans = transDeclarado - transEsperado;
        double diffTotal = totalDeclarado - totalEsperado;

        boolean ok = Alertas.confirmar("Cerrar Caja",
            "── Declarado ──" +
            "\nEfectivo: " + fmt.format(efDeclarado) + "  (Esperado: " + fmt.format(efEsperado) + " | " + (diffEfectivo >= 0 ? "+" : "") + fmt.format(diffEfectivo) + ")" +
            "\nTarjeta: " + fmt.format(tarDeclarado) + "  (Esperado: " + fmt.format(tarEsperado) + " | " + (diffTarjeta >= 0 ? "+" : "") + fmt.format(diffTarjeta) + ")" +
            "\nTransf.: " + fmt.format(transDeclarado) + "  (Esperado: " + fmt.format(transEsperado) + " | " + (diffTrans >= 0 ? "+" : "") + fmt.format(diffTrans) + ")" +
            "\n────────────────" +
            "\nTotal Declarado: " + fmt.format(totalDeclarado) +
            "\nTotal Esperado: " + fmt.format(totalEsperado) +
            "\nDiferencia: " + (diffTotal >= 0 ? "+" : "") + fmt.format(diffTotal) +
            "\n\n¿Cerrar caja?");
        if (!ok) return;

        if (CajaDAO.cerrarCaja(c.getIdCaja(), efDeclarado, tarDeclarado, transDeclarado, totalDeclarado, diffTotal)) {
            Alertas.exito("Caja cerrada", "Caja cerrada exitosamente.\nDeclarado: " + fmt.format(totalDeclarado) +
                "\nEsperado: " + fmt.format(totalEsperado) +
                "\nDiferencia: " + (diffTotal >= 0 ? "+" : "") + fmt.format(diffTotal));
            limpiarCamposCaja();
            cargarEstadoCaja();
            cargarCierres();
        } else {
            Alertas.error("Error", "No se pudo cerrar la caja.");
        }
    }

    @FXML void verResumenCaja() {
        if (!CajaDAO.hayCajaAbierta()) { Alertas.informacion("Sin caja abierta", "No hay una caja abierta actualmente."); return; }
        Caja c = CajaDAO.obtenerCajaAbierta();
        if (c == null) return;
        double ef = parseDouble(txtCajaEfectivo.getText());
        double tar = parseDouble(txtCajaTarjeta.getText());
        double trans = parseDouble(txtCajaTransferencia.getText());
        Alertas.informacion("Resumen de Caja",
            "Apertura: " + c.getFechaApertura().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
            "\nUsuario: " + c.getUsuario() +
            "\nMonto Inicial: " + fmt.format(c.getMontoInicial()) +
            "\n\n--- Declarado ---" +
            "\nEfectivo: " + fmt.format(ef) +
            "\nTarjeta: " + fmt.format(tar) +
            "\nTransferencia: " + fmt.format(trans) +
            "\nTotal: " + fmt.format(ef + tar + trans));
    }

    @FXML void exportarCaja() {
        if (!CajaDAO.hayCajaAbierta()) {
            Alertas.error("Sin caja abierta", "Debe tener una caja abierta para exportar el resumen.");
            return;
        }
        Caja c = CajaDAO.obtenerCajaAbierta();
        if (c == null) return;
        double efDeclarado = parseDouble(txtCajaEfectivo.getText());
        double tarDeclarado = parseDouble(txtCajaTarjeta.getText());
        double transDeclarado = parseDouble(txtCajaTransferencia.getText());
        Map<String, Double> esperado = VentaDAO.obtenerTotalesHoyPorMetodo();
        try {
            java.nio.file.Path pdf = FacturaService.generarCajaPdf(c, efDeclarado, tarDeclarado, transDeclarado,
                esperado.getOrDefault("Efectivo", 0.0),
                esperado.getOrDefault("Tarjeta", 0.0),
                esperado.getOrDefault("Transferencia", 0.0));
            if (Desktop.isDesktopSupported()) {
                try { Desktop.getDesktop().open(pdf.toFile()); }
                catch (Exception e) { System.out.println("[Caja] No se pudo abrir PDF: " + e.getMessage()); }
            }
            Alertas.exito("Exportado", "PDF guardado en:\n" + pdf.toAbsolutePath());
        } catch (Exception e) {
            Alertas.error("Error", "No se pudo exportar PDF: " + e.getMessage());
        }
    }

    @FXML void limpiarCamposCaja() {
        txtCajaEfectivo.setText("0"); txtCajaTarjeta.setText("0"); txtCajaTransferencia.setText("0");
    }

    private void setDiffStyle(Label lbl, double diff) {
        if (Math.abs(diff) < 0.01) lbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#bbf7d0;");
        else if (diff < 0) lbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#fca5a5;");
        else lbl.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#fcd34d;");
    }

    private void calcCajaTotales() {
        double efDeclarado = parseDouble(txtCajaEfectivo.getText());
        double tarDeclarado = parseDouble(txtCajaTarjeta.getText());
        double transDeclarado = parseDouble(txtCajaTransferencia.getText());
        double totalDeclarado = efDeclarado + tarDeclarado + transDeclarado;

        Map<String, Double> esperado = VentaDAO.obtenerTotalesHoyPorMetodo();
        double efEsperado = esperado.getOrDefault("Efectivo", 0.0);
        double tarEsperado = esperado.getOrDefault("Tarjeta", 0.0);
        double transEsperado = esperado.getOrDefault("Transferencia", 0.0);

        lblCajaEfectivo.setText(fmt.format(efDeclarado));
        lblCajaEfectivoEsp.setText(fmt.format(efEsperado));
        double diffEf = efDeclarado - efEsperado;
        lblCajaEfectivoDiff.setText((diffEf >= 0 ? "+" : "") + fmt.format(diffEf));
        setDiffStyle(lblCajaEfectivoDiff, diffEf);

        lblCajaTarjeta.setText(fmt.format(tarDeclarado));
        lblCajaTarjetaEsp.setText(fmt.format(tarEsperado));
        double diffTar = tarDeclarado - tarEsperado;
        lblCajaTarjetaDiff.setText((diffTar >= 0 ? "+" : "") + fmt.format(diffTar));
        setDiffStyle(lblCajaTarjetaDiff, diffTar);

        lblCajaTransferencia.setText(fmt.format(transDeclarado));
        lblCajaTransferenciaEsp.setText(fmt.format(transEsperado));
        double diffTrans = transDeclarado - transEsperado;
        lblCajaTransferenciaDiff.setText((diffTrans >= 0 ? "+" : "") + fmt.format(diffTrans));
        setDiffStyle(lblCajaTransferenciaDiff, diffTrans);

        lblCajaTotalGeneral.setText(fmt.format(totalDeclarado));
        lblCajaTotalGeneralEsp.setText(fmt.format(efEsperado + tarEsperado + transEsperado));

        double diffTotal = totalDeclarado - (efEsperado + tarEsperado + transEsperado);
        String diffStr = (diffTotal >= 0 ? "+" : "") + fmt.format(diffTotal);
        lblCajaDiferencia.setText(diffStr);
        lblCajaDiffForm.setText(diffStr);
        String diffColor;
        if (Math.abs(diffTotal) < 0.01) diffColor = "#bbf7d0";
        else if (diffTotal < 0) diffColor = "#fca5a5";
        else diffColor = "#fcd34d";
        lblCajaDiferencia.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:" + diffColor + ";");
        lblCajaDiffForm.setStyle("-fx-font-size:16px; -fx-font-weight:bold; -fx-text-fill:" + diffColor.replace("bbf7d0", "059669").replace("fca5a5", "dc2626").replace("fcd34d", "d97706") + ";");
    }

    private void cargarCierres() {
        tablaCierres.setItems(FXCollections.observableArrayList(CajaDAO.listarCierres()));
    }

    // ── PEDIDOS ───────────────────────────────────────────────────────────────

    private void configurarColumnasPedidos() {
        colPId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getIdPedido())));
        colPFactura.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getIdFactura()));
        colPCliente.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getClienteNombre()));
        colPItems.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getItemsTexto()));
        colPTotal.setCellValueFactory(cd -> new SimpleStringProperty(fmt.format(cd.getValue().getTotal())));
        colPEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstadoEmoji() + " " + cd.getValue().getEstado()));
        colPFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        colPTipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTipoVenta()));
    }

    @FXML void filtrarPedidos() {
        cargarPedidos();
    }

    @FXML void limpiarFiltrosPedidos() {
        cmbFiltroCliente.setValue(null);
        cmbFiltroEstado.setValue("Todos");
        txtBuscarPedido.clear();
        cargarPedidos();
    }

    private void cargarPedidos() {
        Cliente cli = cmbFiltroCliente.getValue();
        String clienteNombre = cli != null ? (cli.getNombre() + " " + (cli.getApellido() != null ? cli.getApellido() : "")).trim() : null;
        String estado = cmbFiltroEstado.getValue();
        String busqueda = txtBuscarPedido.getText();
        tablaPedidos.setItems(FXCollections.observableArrayList(PedidoVentaDAO.listar(clienteNombre, estado, busqueda)));
    }

    private PedidoVenta pedidoSeleccionado;

    private void mostrarDetallePedido(PedidoVenta p) {
        this.pedidoSeleccionado = p;
        if (p == null) { ocultarDetallePedido(); return; }
        panelDetallePedido.setVisible(true);
        panelDetallePedido.setManaged(true);
        lblDetalleFactura.setText("Factura: " + p.getIdFactura());
        lblDetalleCliente.setText("Cliente: " + p.getClienteNombre());
        lblDetalleItems.setText("Items: " + (p.getItemsTexto() != null ? p.getItemsTexto() : "--"));
        lblDetalleFecha.setText("Fecha: " + p.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblDetalleTotal.setText("Total: " + fmt.format(p.getTotal()));
        lblDetalleTipo.setText("Tipo: " + p.getTipoVenta());
        lblDetallePago.setText("Pago: " + (p.getMetodoPago() != null ? p.getMetodoPago() : "--"));
        lblDetalleObservaciones.setText("Obs: " + (p.getObservaciones() != null ? p.getObservaciones() : "--"));

        cmbPedidoCliente.setValue(null);
        for (Cliente c : cmbPedidoCliente.getItems()) {
            String fullName = c.getNombre() + " " + (c.getApellido() != null ? c.getApellido() : "");
            if (fullName.trim().equalsIgnoreCase(p.getClienteNombre().trim())) {
                cmbPedidoCliente.setValue(c);
                break;
            }
        }

        boolean terminado = "Cancelado".equals(p.getEstado());
        btnCambiarEstado.setDisable(terminado);
        btnCobrarPedido.setDisable(!"Pendiente".equals(p.getEstado()));
        btnCancelarPedido.setDisable(terminado);
    }

    @FXML void asignarClientePedido() {
        PedidoVenta p = pedidoSeleccionado;
        if (p == null) return;
        Cliente c = cmbPedidoCliente.getValue();
        if (c == null) { Alertas.error("Seleccione un cliente", "Seleccione un cliente de la lista."); return; }
        String newName = c.getNombre() + " " + (c.getApellido() != null ? c.getApellido() : "");
        boolean ok = Alertas.confirmar("Asignar Cliente", "Asignar cliente '" + newName.trim() + "' al pedido " + p.getIdFactura() + "?");
        if (!ok) return;
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            try (PreparedStatement ps = conn.prepareStatement("UPDATE tbl_PEDIDO_VENTA SET cliente_nombre = ? WHERE id_pedido = ?")) {
                ps.setString(1, newName.trim());
                ps.setInt(2, p.getIdPedido());
                if (ps.executeUpdate() > 0) {
                    p.setClienteNombre(newName.trim());
                    cargarPedidos();
                    mostrarDetallePedido(p);
                    Alertas.exito("Cliente asignado", "Cliente actualizado a: " + newName.trim());
                }
            }
        } catch (SQLException e) {
            Alertas.error("Error", "No se pudo asignar cliente: " + e.getMessage());
        }
    }

    private void ocultarDetallePedido() {
        panelDetallePedido.setVisible(false);
        panelDetallePedido.setManaged(false);
    }

    @FXML void cambiarEstadoPedido() {
        PedidoVenta p = tablaPedidos.getSelectionModel().getSelectedItem();
        if (p == null) { Alertas.error("Seleccione un pedido", "Seleccione un pedido de la tabla primero."); return; }
        String[] estados;
        if ("Cancelado".equals(p.getEstado())) return;
        switch (p.getEstado()) {
            case "Pendiente" -> estados = new String[]{"Preparando", "Listo", "En camino", "Entregado", "Cancelado"};
            case "Preparando" -> estados = new String[]{"Listo", "En camino", "Entregado", "Cancelado"};
            case "Listo" -> estados = new String[]{"En camino", "Entregado", "Cancelado"};
            case "En camino" -> estados = new String[]{"Entregado", "Cancelado"};
            case "Entregado" -> estados = new String[]{"Cancelado"};
            default -> estados = new String[]{};
        }
        if (estados.length == 0) return;

        ChoiceDialog<String> dialog = new ChoiceDialog<>(estados[0], estados);
        dialog.setTitle("Cambiar Estado");
        dialog.setHeaderText("Pedido: " + p.getIdFactura());
        dialog.setContentText("Nuevo estado:");
        dialog.showAndWait().ifPresent(nuevoEstado -> {
            if (PedidoVentaDAO.actualizarEstado(p.getIdPedido(), nuevoEstado)) {
                cargarPedidos();
                PedidoVenta actualizado = PedidoVentaDAO.obtenerPorId(p.getIdPedido());
                mostrarDetallePedido(actualizado);
            } else {
                Alertas.error("Error", "No se pudo actualizar el estado.");
            }
        });
    }

    @FXML void cobrarPedido() {
        PedidoVenta p = tablaPedidos.getSelectionModel().getSelectedItem();
        if (p == null) { Alertas.error("Seleccione un pedido", "Seleccione un pedido de la tabla primero."); return; }
        if (!"Pendiente".equals(p.getEstado())) { Alertas.error("No disponible", "Solo pedidos Pendientes pueden cobrarse."); return; }
        Alertas.informacion("Cobrar Pedido", "Redirigiendo al POS para cobrar el pedido " + p.getIdFactura() + "...\n\nTotal: " + fmt.format(p.getTotal()));
        mostrarVenta();
    }

    @FXML void cancelarPedido() {
        PedidoVenta p = tablaPedidos.getSelectionModel().getSelectedItem();
        if (p == null) { Alertas.error("Seleccione un pedido", "Seleccione un pedido de la tabla primero."); return; }
        if ("Entregado".equals(p.getEstado()) || "Cancelado".equals(p.getEstado())) return;
        boolean ok = Alertas.confirmar("Cancelar Pedido", "¿Está seguro de cancelar el pedido " + p.getIdFactura() + "?");
        if (!ok) return;
        if (PedidoVentaDAO.actualizarEstado(p.getIdPedido(), "Cancelado")) {
            cargarPedidos();
            ocultarDetallePedido();
        } else {
            Alertas.error("Error", "No se pudo cancelar el pedido.");
        }
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.trim().replace(",", "")); } catch (Exception e) { return 0; }
    }
}
