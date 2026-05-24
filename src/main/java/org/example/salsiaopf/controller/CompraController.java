package org.example.salsiaopf.controller;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.example.salsiaopf.dao.CompraDAO;
import org.example.salsiaopf.dao.ProveedorDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class CompraController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;

    // ScrollPanes
    @FXML private ScrollPane scrollOrdenCompra, scrollProveedores, scrollRecepcion;

    // ── ORDEN DE COMPRA ────────────────────────────────────────────────
    @FXML private TextField txtCodigoOrden, txtCantidad, txtUnidad, txtPrecioUnitario;
    @FXML private DatePicker dateFechaOrden;
    @FXML private ComboBox<ItemCombo> cmbProveedorOrden, cmbIngrediente;
    @FXML private TextArea txtNotasOrden;
    @FXML private TableView<DetalleRow> tablaDetalle;
    @FXML private Label lblMetricaPendientes, lblMetricaRecibidas, lblMetricaTotal;
    @FXML private Label lblResCodigo, lblResProveedor, lblResFecha, lblResItems, lblResTotal;
    @FXML private Label lblTotalDetalle;
    private final ObservableList<DetalleRow> detallesOrden = FXCollections.observableArrayList();

    // ── PROVEEDORES ────────────────────────────────────────────────────
    @FXML private TextField txtIdProveedor, txtNombreProveedor, txtRncProveedor;
    @FXML private TextField txtTelefonoProveedor, txtDireccionProveedor, txtCorreoProveedor;
    @FXML private TableView<ProveedorRow> tablaProveedores;
    private final ObservableList<ProveedorRow> proveedoresList = FXCollections.observableArrayList();

    // ── RECEPCIÓN ──────────────────────────────────────────────────────
    @FXML private ComboBox<ItemCombo> cmbOrdenRecepcion;
    @FXML private TextField txtRecCodigo, txtRecProveedor, txtRecFecha, txtRecEstado, txtRecTotal;
    @FXML private TextArea txtRecNotas;
    @FXML private TextField txtPagoMonto;
    @FXML private DatePicker datePago;
    @FXML private ComboBox<String> cmbMetodoPago;
    @FXML private TableView<RecepcionRow> tablaRecepcion;
    @FXML private Label lblRecPendientes, lblRecRecibidas, lblRecPendientesPago;
    private final ObservableList<RecepcionRow> recepcionList = FXCollections.observableArrayList();
    private int idOrdenSeleccionada = -1;

    @FXML
    private void initialize() {
        ControllerUtil.cargarLogo(logoImage);
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
        CompraDAO.crearTablasSiNoExisten();
        ProveedorDAO.crearTablaSiNoExiste();

        configurarTablaDetalle();
        configurarTablaProveedores();
        configurarTablaRecepcion();

        dateFechaOrden.setValue(LocalDate.now());
        datePago.setValue(LocalDate.now());
        cmbMetodoPago.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta de crédito", "Transferencia bancaria", "Cheque"));

        cargarProveedoresEnCombo();
        cargarIngredientesEnCombo();
        cargarMetricas();
        generarCodigo();

        cmbIngrediente.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) txtUnidad.setText(sel.dato);
        });

        cmbOrdenRecepcion.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) cargarDetalleOrdenRecepcion(sel.id);
        });

        Platform.runLater(() -> mostrarOrdenCompra());
    }

    // ── VISIBILIDAD ────────────────────────────────────────────────────

    private void ocultarTodas() {
        for (ScrollPane sp : new ScrollPane[]{scrollOrdenCompra, scrollProveedores, scrollRecepcion}) {
            if (sp != null) { sp.setVisible(false); sp.setManaged(false); }
        }
    }

    private void mostrarVista(ScrollPane sp) {
        ocultarTodas();
        if (sp != null) { sp.setVisible(true); sp.setManaged(true); }
    }

    @FXML private void mostrarOrdenCompra() { mostrarVista(scrollOrdenCompra); cargarMetricas(); generarCodigo(); }
    @FXML private void mostrarProveedores() { mostrarVista(scrollProveedores); cargarProveedoresTabla(); }
    @FXML private void mostrarRecepcion() { mostrarVista(scrollRecepcion); recargarRecepcion(); }
    @FXML private void mostrarNotificaciones() { Alertas.advertencia("Notificaciones", "En desarrollo."); }

    // ═══════════════════════════════════════════════════════════════════
    //  ORDEN DE COMPRA
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaDetalle() {
        TableColumn<DetalleRow, String> c1 = new TableColumn<>("Ingrediente");
        c1.setCellValueFactory(d -> d.getValue().nombre);
        TableColumn<DetalleRow, Double> c2 = new TableColumn<>("Cantidad");
        c2.setCellValueFactory(d -> d.getValue().cantidad.asObject());
        TableColumn<DetalleRow, String> c3 = new TableColumn<>("Unidad");
        c3.setCellValueFactory(d -> d.getValue().unidad);
        TableColumn<DetalleRow, Double> c4 = new TableColumn<>("Precio uni.");
        c4.setCellValueFactory(d -> d.getValue().precio.asObject());
        TableColumn<DetalleRow, Double> c5 = new TableColumn<>("Subtotal");
        c5.setCellValueFactory(d -> d.getValue().subtotal.asObject());
        TableColumn<DetalleRow, Void> c6 = new TableColumn<>("Acción");
        c6.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✖");
            { btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 4px 10px;"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                btn.setOnAction(e -> { detallesOrden.remove(getIndex()); actualizarResumen(); });
                setGraphic(btn);
            }
        });
        tablaDetalle.getColumns().setAll(c1, c2, c3, c4, c5, c6);
        tablaDetalle.setItems(detallesOrden);
    }

    private void cargarProveedoresEnCombo() {
        List<Object[]> lista = ProveedorDAO.listarProveedores();
        cmbProveedorOrden.setItems(FXCollections.observableArrayList());
        for (Object[] p : lista) cmbProveedorOrden.getItems().add(new ItemCombo((int) p[0], (String) p[1], ""));
        cmbOrdenRecepcion.setItems(FXCollections.observableArrayList());
    }

    private void cargarIngredientesEnCombo() {
        List<Object[]> lista = new java.util.ArrayList<>();
        String sql = "SELECT ID_ingredientes, Nom_Ingrediente, UM FROM tbl_INGREDIENTE ORDER BY Nom_Ingrediente";
        try (Connection conn = org.example.salsiaopf.database.ConexionBD.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (conn != null) while (rs.next()) lista.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
        } catch (SQLException e) { System.out.println("[Compras] Error ingredientes: " + e.getMessage()); }
        cmbIngrediente.setItems(FXCollections.observableArrayList());
        for (Object[] i : lista) cmbIngrediente.getItems().add(new ItemCombo((int) i[0], (String) i[1], (String) i[2]));
    }

    private void generarCodigo() {
        txtCodigoOrden.setText(CompraDAO.generarCodigoOrden());
        actualizarResumen();
    }

    @FXML
    private void agregarDetalle() {
        ItemCombo ing = cmbIngrediente.getValue();
        if (ing == null) { Alertas.advertencia("Validación", "Seleccione un ingrediente."); return; }
        String cantStr = txtCantidad.getText().trim();
        String precStr = txtPrecioUnitario.getText().trim();
        if (cantStr.isEmpty() || precStr.isEmpty()) { Alertas.advertencia("Validación", "Ingrese cantidad y precio."); return; }
        try {
            double cant = Double.parseDouble(cantStr);
            double prec = Double.parseDouble(precStr);
            if (cant <= 0 || prec < 0) throw new NumberFormatException();
            String unidad = txtUnidad.getText().trim();
            if (unidad.isEmpty()) unidad = ing.dato;
            detallesOrden.add(new DetalleRow(ing.id, ing.nombre, cant, unidad, prec));
            cmbIngrediente.setValue(null); txtCantidad.clear(); txtUnidad.clear(); txtPrecioUnitario.clear();
            actualizarResumen();
        } catch (NumberFormatException e) {
            Alertas.advertencia("Validación", "Cantidad y precio deben ser números válidos (cant > 0, precio >= 0).");
        }
    }

    @FXML
    private void quitarDetalle() {
        DetalleRow sel = tablaDetalle.getSelectionModel().getSelectedItem();
        if (sel != null) { detallesOrden.remove(sel); actualizarResumen(); }
    }

    private void actualizarResumen() {
        double total = 0;
        for (DetalleRow d : detallesOrden) total += d.subtotal.get();
        lblResCodigo.setText(txtCodigoOrden.getText());
        ItemCombo prov = cmbProveedorOrden.getValue();
        lblResProveedor.setText(prov != null ? prov.nombre : "---");
        lblResFecha.setText(dateFechaOrden.getValue() != null ? dateFechaOrden.getValue().toString() : "---");
        lblResItems.setText(String.valueOf(detallesOrden.size()));
        String totalStr = String.format("RD$ %,.2f", total);
        lblResTotal.setText(totalStr);
        lblTotalDetalle.setText("Total: " + totalStr);
    }

    @FXML
    private void guardarOrdenCompra() {
        ItemCombo prov = cmbProveedorOrden.getValue();
        if (prov == null) { Alertas.advertencia("Validación", "Seleccione un proveedor."); return; }
        LocalDate fecha = dateFechaOrden.getValue();
        if (fecha == null) { Alertas.advertencia("Validación", "Seleccione una fecha."); return; }
        if (detallesOrden.isEmpty()) { Alertas.advertencia("Validación", "Agregue al menos un ingrediente."); return; }

        double total = 0;
        java.util.List<CompraDAO.DetalleOrden> detalles = new java.util.ArrayList<>();
        for (DetalleRow d : detallesOrden) {
            detalles.add(new CompraDAO.DetalleOrden(d.idIngrediente, d.cantidad.get(), d.unidad.get(), d.precio.get()));
            total += d.subtotal.get();
        }

        int id = CompraDAO.guardarOrden(prov.id, fecha, txtNotasOrden.getText().trim(), total, detalles);
        if (id > 0) {
            Alertas.exito("Orden de compra", "Orden guardada exitosamente.\nCódigo: " + txtCodigoOrden.getText());
            nuevaOrdenCompra();
            cargarMetricas();
        } else {
            Alertas.error("Error", "No se pudo guardar la orden. Verifique la conexión.");
        }
    }

    @FXML
    private void nuevaOrdenCompra() {
        cmbProveedorOrden.setValue(null);
        dateFechaOrden.setValue(LocalDate.now());
        txtNotasOrden.clear();
        detallesOrden.clear();
        txtCantidad.clear(); txtUnidad.clear(); txtPrecioUnitario.clear();
        cmbIngrediente.setValue(null);
        generarCodigo();
    }

    @FXML
    private void limpiarOrdenCompra() { nuevaOrdenCompra(); }

    private void cargarMetricas() {
        List<Object[]> ords = CompraDAO.listarOrdenes();
        int pend = 0, rec = 0;
        double tot = 0;
        for (Object[] o : ords) {
            String est = (String) o[4];
            if ("Pendiente".equals(est)) pend++;
            else if ("Recibida".equals(est)) rec++;
            tot += (double) o[5];
        }
        lblMetricaPendientes.setText(String.valueOf(pend));
        lblMetricaRecibidas.setText(String.valueOf(rec));
        lblMetricaTotal.setText(String.format("RD$ %,.0f", tot));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PROVEEDORES
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaProveedores() {
        TableColumn<ProveedorRow, Integer> ci = new TableColumn<>("ID");
        ci.setCellValueFactory(d -> d.getValue().id.asObject());
        TableColumn<ProveedorRow, String> cn = new TableColumn<>("Nombre");
        cn.setCellValueFactory(d -> d.getValue().nombre);
        TableColumn<ProveedorRow, String> cr = new TableColumn<>("RNC");
        cr.setCellValueFactory(d -> d.getValue().rnc);
        TableColumn<ProveedorRow, String> ct = new TableColumn<>("Teléfono");
        ct.setCellValueFactory(d -> d.getValue().telefono);
        TableColumn<ProveedorRow, String> cd = new TableColumn<>("Dirección");
        cd.setCellValueFactory(d -> d.getValue().direccion);
        TableColumn<ProveedorRow, String> cc = new TableColumn<>("Correo");
        cc.setCellValueFactory(d -> d.getValue().correo);
        tablaProveedores.getColumns().setAll(ci, cn, cr, ct, cd, cc);
        tablaProveedores.setItems(proveedoresList);
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtIdProveedor.setText(String.valueOf(sel.id.get()));
                txtNombreProveedor.setText(sel.nombre.get());
                txtRncProveedor.setText(sel.rnc.get());
                txtTelefonoProveedor.setText(sel.telefono.get());
                txtDireccionProveedor.setText(sel.direccion.get());
                txtCorreoProveedor.setText(sel.correo.get());
            }
        });
    }

    private void cargarProveedoresTabla() {
        proveedoresList.clear();
        for (Object[] p : ProveedorDAO.listarProveedores()) {
            proveedoresList.add(new ProveedorRow((int) p[0], (String) p[1], (String) p[4], (String) p[2], (String) p[3], (String) p[5]));
        }
    }

    @FXML
    private void guardarProveedor() {
        String nombre = txtNombreProveedor.getText().trim();
        if (nombre.isEmpty()) { Alertas.advertencia("Validación", "El nombre del proveedor es obligatorio."); return; }
        String rnc = txtRncProveedor.getText().trim();
        String tel = txtTelefonoProveedor.getText().trim();
        String dir = txtDireccionProveedor.getText().trim();
        String cor = txtCorreoProveedor.getText().trim();
        String idStr = txtIdProveedor.getText().trim();

        boolean ok;
        if (idStr.isEmpty()) {
            ok = ProveedorDAO.guardarProveedor(nombre, tel, dir, rnc, cor) > 0;
        } else {
            ok = ProveedorDAO.actualizarProveedor(Integer.parseInt(idStr), nombre, tel, dir, rnc, cor);
        }
        if (ok) {
            Alertas.exito("Proveedores", "Proveedor guardado correctamente.");
            cargarProveedoresTabla();
            cargarProveedoresEnCombo();
            limpiarProveedorForm();
        } else {
            Alertas.error("Error", "No se pudo guardar el proveedor.");
        }
    }

    @FXML private void nuevoProveedor() { limpiarProveedorForm(); }
    @FXML private void limpiarProveedor() { limpiarProveedorForm(); }

    private void limpiarProveedorForm() {
        txtIdProveedor.clear(); txtNombreProveedor.clear(); txtRncProveedor.clear();
        txtTelefonoProveedor.clear(); txtDireccionProveedor.clear(); txtCorreoProveedor.clear();
    }

    @FXML
    private void editarProveedor() {
        ProveedorRow sel = tablaProveedores.getSelectionModel().getSelectedItem();
        if (sel == null) { Alertas.advertencia("Editar", "Seleccione un proveedor de la tabla."); return; }
        txtIdProveedor.setText(String.valueOf(sel.id.get()));
        txtNombreProveedor.setText(sel.nombre.get());
        txtRncProveedor.setText(sel.rnc.get());
        txtTelefonoProveedor.setText(sel.telefono.get());
        txtDireccionProveedor.setText(sel.direccion.get());
        txtCorreoProveedor.setText(sel.correo.get());
    }

    @FXML
    private void eliminarProveedor() {
        ProveedorRow sel = tablaProveedores.getSelectionModel().getSelectedItem();
        if (sel == null) { Alertas.advertencia("Eliminar", "Seleccione un proveedor de la tabla."); return; }
        if (Alertas.confirmar("Eliminar proveedor", "¿Seguro de eliminar a " + sel.nombre.get() + "?\nEsta acción no se puede deshacer.")) {
            if (ProveedorDAO.eliminarProveedor(sel.id.get())) {
                Alertas.exito("Proveedores", "Proveedor eliminado.");
                cargarProveedoresTabla();
                cargarProveedoresEnCombo();
                if (txtIdProveedor.getText().equals(String.valueOf(sel.id.get()))) limpiarProveedorForm();
            } else {
                Alertas.error("Error", "No se pudo eliminar. El proveedor podría tener órdenes asociadas.");
            }
        }
    }

    @FXML private void actualizarProveedores() { cargarProveedoresTabla(); cargarProveedoresEnCombo(); }

    // ═══════════════════════════════════════════════════════════════════
    //  RECEPCIÓN
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaRecepcion() {
        TableColumn<RecepcionRow, String> c1 = new TableColumn<>("Código");
        c1.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<RecepcionRow, String> c2 = new TableColumn<>("Proveedor");
        c2.setCellValueFactory(d -> d.getValue().proveedor);
        TableColumn<RecepcionRow, String> c3 = new TableColumn<>("Fecha");
        c3.setCellValueFactory(d -> d.getValue().fecha);
        TableColumn<RecepcionRow, String> c4 = new TableColumn<>("Total");
        c4.setCellValueFactory(d -> d.getValue().total);
        TableColumn<RecepcionRow, String> c5 = new TableColumn<>("Estado");
        c5.setCellValueFactory(d -> d.getValue().estado);
        TableColumn<RecepcionRow, Void> c6 = new TableColumn<>("Acciones");
        c6.setCellFactory(col -> new TableCell<>() {
            private final Button btnRecibir = new Button("✔ Recibir");
            private final Button btnPagar = new Button("💳 Pagar");
            { 
                btnRecibir.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                btnPagar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                RecepcionRow r = getTableView().getItems().get(getIndex());
                if ("Pendiente".equals(r.estadoRaw)) {
                    btnRecibir.setOnAction(e -> {
                        if (CompraDAO.marcarRecibida(r.id)) {
                            Alertas.exito("Recepción", "Orden " + r.codigo.get() + " marcada como recibida.");
                            recargarRecepcion();
                        } else { Alertas.error("Error", "No se pudo marcar como recibida."); }
                    });
                    setGraphic(btnRecibir);
                } else {
                    btnPagar.setOnAction(e -> {
                        txtPagoMonto.setText(r.totalRaw.replace("RD$", "").replace(",", "").trim());
                        cmbOrdenRecepcion.setValue(new ItemCombo(r.id, r.codigo.get() + " - " + r.proveedor.get(), ""));
                        cargarDetalleOrdenRecepcion(r.id);
                        mostrarVista(scrollRecepcion);
                        Alertas.informacion("Pago", "Datos de la orden cargados para procesar pago.");
                    });
                    setGraphic(btnPagar);
                }
            }
        });
        tablaRecepcion.getColumns().setAll(c1, c2, c3, c4, c5, c6);
        tablaRecepcion.setItems(recepcionList);
    }

    @FXML
    private void recargarRecepcion() {
        List<Object[]> ords = CompraDAO.listarOrdenes();
        cmbOrdenRecepcion.setItems(FXCollections.observableArrayList());
        recepcionList.clear();
        int pend = 0, rec = 0, pendPago = 0;
        for (Object[] o : ords) {
            int id = (int) o[0];
            String cod = (String) o[1];
            String prov = (String) o[2];
            String fecha = o[3] != null ? o[3].toString() : "";
            String est = (String) o[4];
            double tot = (double) o[5];
            recepcionList.add(new RecepcionRow(id, cod, prov, fecha, tot, est));
            if ("Pendiente".equals(est)) { pend++; cmbOrdenRecepcion.getItems().add(new ItemCombo(id, cod + " - " + prov + " - " + fecha, "")); }
            else if ("Recibida".equals(est)) rec++;
        }
        lblRecPendientes.setText(String.valueOf(pend));
        lblRecRecibidas.setText(String.valueOf(rec));
        lblRecPendientesPago.setText(String.valueOf(pend));
    }

    private void cargarDetalleOrdenRecepcion(int idOrden) {
        idOrdenSeleccionada = idOrden;
        Object[] ord = CompraDAO.obtenerOrden(idOrden);
        if (ord == null) return;
        txtRecCodigo.setText((String) ord[1]);
        txtRecProveedor.setText((String) ord[3]);
        txtRecFecha.setText(ord[4] != null ? ord[4].toString() : "");
        txtRecEstado.setText((String) ord[6]);
        txtRecTotal.setText(String.format("RD$ %,.2f", (double) ord[7]));
        txtRecNotas.setText((String) ord[5]);
        txtPagoMonto.setText(String.format("%.2f", (double) ord[7]));
        datePago.setValue(LocalDate.now());
    }

    @FXML
    private void confirmarRecepcion() {
        if (idOrdenSeleccionada <= 0) { Alertas.advertencia("Recepción", "Seleccione una orden pendiente."); return; }
        if (CompraDAO.marcarRecibida(idOrdenSeleccionada)) {
            Alertas.exito("Recepción", "Orden recibida y confirmada exitosamente.");
            recargarRecepcion();
            idOrdenSeleccionada = -1;
        } else {
            Alertas.error("Error", "No se pudo marcar como recibida. Puede que ya esté recibida o cancelada.");
        }
    }

    @FXML
    private void procesarPago() {
        if (idOrdenSeleccionada <= 0) { Alertas.advertencia("Pago", "Seleccione una orden primero."); return; }
        String montoStr = txtPagoMonto.getText().trim();
        if (montoStr.isEmpty()) { Alertas.advertencia("Pago", "Ingrese el monto a pagar."); return; }
        double monto;
        try { monto = Double.parseDouble(montoStr.replace(",", "")); } catch (NumberFormatException e) { Alertas.advertencia("Pago", "Monto inválido."); return; }
        String metodo = cmbMetodoPago.getValue();
        if (metodo == null) { Alertas.advertencia("Pago", "Seleccione un método de pago."); return; }
        LocalDate fecha = datePago.getValue();
        if (fecha == null) { Alertas.advertencia("Pago", "Seleccione la fecha de pago."); return; }

        int r = CompraDAO.guardarPago(idOrdenSeleccionada, fecha, monto, metodo);
        if (r > 0) {
            Alertas.exito("Pago", "Pago procesado exitosamente.\nMonto: RD$ " + String.format("%,.2f", monto) + "\nMétodo: " + metodo);
            txtPagoMonto.clear();
            cmbMetodoPago.setValue(null);
            recargarRecepcion();
        } else {
            Alertas.error("Error", "No se pudo procesar el pago. Verifique la conexión.");
        }
    }

    // ── NAVEGACIÓN ──────────────────────────────────────────────────────

    @FXML
    private void volverMenu(ActionEvent event) { Navegacion.volverCentroSistema(event); }
    @FXML
    private void salirSistema(ActionEvent event) { System.exit(0); }

    // ═══════════════════════════════════════════════════════════════════
    //  CLASES INTERNAS (Modelos para UI)
    // ═══════════════════════════════════════════════════════════════════

    public static class ItemCombo {
        public final int id;
        public final String nombre, dato;
        public ItemCombo(int id, String nombre, String dato) { this.id = id; this.nombre = nombre; this.dato = dato; }
        @Override public String toString() { return nombre; }
    }

    public static class DetalleRow {
        public final int idIngrediente;
        public final SimpleStringProperty nombre, unidad;
        public final SimpleDoubleProperty cantidad, precio, subtotal;
        public DetalleRow(int idIng, String nom, double cant, String uni, double prec) {
            idIngrediente = idIng;
            nombre = new SimpleStringProperty(nom);
            cantidad = new SimpleDoubleProperty(cant);
            unidad = new SimpleStringProperty(uni);
            precio = new SimpleDoubleProperty(prec);
            subtotal = new SimpleDoubleProperty(cant * prec);
        }
    }

    public static class ProveedorRow {
        public final SimpleIntegerProperty id;
        public final SimpleStringProperty nombre, rnc, telefono, direccion, correo;
        public ProveedorRow(int id, String nombre, String rnc, String tel, String dir, String cor) {
            this.id = new SimpleIntegerProperty(id);
            this.nombre = new SimpleStringProperty(nombre);
            this.rnc = new SimpleStringProperty(rnc);
            this.telefono = new SimpleStringProperty(tel);
            this.direccion = new SimpleStringProperty(dir);
            this.correo = new SimpleStringProperty(cor);
        }
    }

    public static class RecepcionRow {
        public final int id;
        public final String estadoRaw, totalRaw;
        public final SimpleStringProperty codigo, proveedor, fecha, total, estado;
        public RecepcionRow(int id, String cod, String prov, String fec, double tot, String est) {
            this.id = id;
            codigo = new SimpleStringProperty(cod);
            proveedor = new SimpleStringProperty(prov);
            fecha = new SimpleStringProperty(fec);
            total = new SimpleStringProperty(String.format("RD$ %,.2f", tot));
            estado = new SimpleStringProperty(est);
            estadoRaw = est;
            totalRaw = String.format("RD$ %,.2f", tot);
        }
    }
}
