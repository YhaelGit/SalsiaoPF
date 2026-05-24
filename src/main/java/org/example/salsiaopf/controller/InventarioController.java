package org.example.salsiaopf.controller;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.example.salsiaopf.dao.InventarioDAO;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class InventarioController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private Button btnNotificaciones;

    // ScrollPanes
    @FXML private ScrollPane scrollDashboard, scrollProductos, scrollEntradas, scrollSalidas, scrollMovimientos, scrollAlertas, scrollCategorias;

    // ── DASHBOARD ──────────────────────────────────────────────────────
    @FXML private TextField txtBuscarProducto;
    @FXML private Label lblTotalProductos, lblStockBajo, lblAgotados, lblVencidos, lblValorInventario;
    @FXML private TableView<ProductoRow> tablaProductos;
    private final ObservableList<ProductoRow> productosList = FXCollections.observableArrayList();

    // ── PRODUCTOS ──────────────────────────────────────────────────────
    @FXML private TextField txtProdCodigo, txtProdNombre, txtProdCantidad, txtProdCosto, txtProdStockMin;
    @FXML private ComboBox<String> cmbProdCategoria, cmbProdUnidad, cmbProdEstado;
    @FXML private DatePicker dateProdVenc;
    @FXML private TextArea txtProdObservacion;
    @FXML private TableView<ProductoRow> tablaProductosCRUD;
    private int idProductoEditando = -1;

    // ── ENTRADAS ───────────────────────────────────────────────────────
    @FXML private ComboBox<ItemCombo> cmbEntradaProducto;
    @FXML private TextField txtEntradaCantidad, txtEntradaObservacion;
    @FXML private DatePicker dateEntrada;
    @FXML private Label lblEntradaInfo;
    @FXML private TableView<MovimientoRow> tablaEntradas;

    // ── SALIDAS ────────────────────────────────────────────────────────
    @FXML private ComboBox<ItemCombo> cmbSalidaProducto;
    @FXML private TextField txtSalidaCantidad, txtSalidaObservacion;
    @FXML private DatePicker dateSalida;
    @FXML private Label lblSalidaInfo;
    @FXML private TableView<MovimientoRow> tablaSalidas;

    // ── MOVIMIENTOS ────────────────────────────────────────────────────
    @FXML private TextField txtBuscarMovimiento;
    @FXML private TableView<MovimientoFullRow> tablaMovimientos;
    private final ObservableList<MovimientoFullRow> movimientosList = FXCollections.observableArrayList();

    // ── ALERTAS ────────────────────────────────────────────────────────
    @FXML private Label lblAlertStockBajo, lblAlertVencidos, lblAlertValorRiesgo;
    @FXML private TableView<StockBajoRow> tablaStockBajo;
    @FXML private TableView<VencidoRow> tablaVencidos;

    // ── CATEGORÍAS ─────────────────────────────────────────────────────
    @FXML private TextField txtCatNombre, txtCatDescripcion;
    @FXML private TableView<CategoriaRow> tablaCategorias;
    private final ObservableList<CategoriaRow> categoriasList = FXCollections.observableArrayList();

    private String usuarioActual = "Admin";

    @FXML
    private void initialize() {
        ControllerUtil.cargarLogo(logoImage);
        ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual);
        InventarioDAO.crearTablasSiNoExisten();

        try { usuarioActual = SessionManager.getInstance().getUsuarioActivo().getNombre(); } catch (Exception ignored) {}

        configurarTablaProductos();
        configurarTablaProductosCRUD();
        configurarTablaMovimientos();
        configurarTablaAlertas();
        configurarTablaCategorias();
        configurarTablasMovimientosLigeras();

        cmbProdUnidad.setItems(FXCollections.observableArrayList("UN", "LB", "KG", "LTR", "ML", "PAQ", "CAJ", "BOL", "GR", "OZ"));
        cmbProdEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo", "Agotado"));
        cmbProdEstado.setValue("Activo");
        cmbProdCategoria.setItems(FXCollections.observableArrayList(InventarioDAO.listarCategorias()));

        cargarDashboard();
        cargarProductos();
        cargarCategorias();
        cargarMovimientos(null);

        dateEntrada.setValue(LocalDate.now());
        dateSalida.setValue(LocalDate.now());

        cmbEntradaProducto.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                Object[] p = InventarioDAO.obtenerProducto(n.id);
                lblEntradaInfo.setText("Stock actual: " + (p != null ? String.format("%.2f %s", (double)p[5], p[4]) : "---"));
            }
        });
        cmbSalidaProducto.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) {
                Object[] p = InventarioDAO.obtenerProducto(n.id);
                lblSalidaInfo.setText("Stock actual: " + (p != null ? String.format("%.2f %s", (double)p[5], p[4]) : "---"));
            }
        });

        Platform.runLater(() -> mostrarDashboard());
    }

    // ── VISIBILIDAD ────────────────────────────────────────────────────

    private void ocultarTodas() {
        for (ScrollPane sp : new ScrollPane[]{scrollDashboard, scrollProductos, scrollEntradas, scrollSalidas, scrollMovimientos, scrollAlertas, scrollCategorias}) {
            if (sp != null) { sp.setVisible(false); sp.setManaged(false); }
        }
    }

    private void mostrarVista(ScrollPane sp) { ocultarTodas(); if (sp != null) { sp.setVisible(true); sp.setManaged(true); } }

    @FXML private void mostrarDashboard() { mostrarVista(scrollDashboard); cargarDashboard(); }
    @FXML private void mostrarProductos() { mostrarVista(scrollProductos); cargarProductos(); cargarCategoriasCombo(); }
    @FXML private void mostrarEntradas() { mostrarVista(scrollEntradas); cargarComboProductos(cmbEntradaProducto); cargarTablaMovimientos(tablaEntradas, "Entrada"); }
    @FXML private void mostrarSalidas() { mostrarVista(scrollSalidas); cargarComboProductos(cmbSalidaProducto); cargarTablaMovimientos(tablaSalidas, "Salida"); }
    @FXML private void mostrarMovimientos() { mostrarVista(scrollMovimientos); cargarMovimientos(null); }
    @FXML private void mostrarAlertas() { mostrarVista(scrollAlertas); cargarAlertas(); }
    @FXML private void mostrarCategorias() { mostrarVista(scrollCategorias); cargarCategorias(); }

    @FXML
    private void mostrarNotificaciones() {
        Alertas.advertencia("Notificaciones", "En desarrollo.");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaProductos() {
        TableColumn<ProductoRow, String> c1 = new TableColumn<>("Código"); c1.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<ProductoRow, String> c2 = new TableColumn<>("Producto"); c2.setCellValueFactory(d -> d.getValue().nombre);
        TableColumn<ProductoRow, String> c3 = new TableColumn<>("Categoría"); c3.setCellValueFactory(d -> d.getValue().categoria);
        TableColumn<ProductoRow, String> c4 = new TableColumn<>("Cantidad"); c4.setCellValueFactory(d -> d.getValue().cantidadStr);
        TableColumn<ProductoRow, String> c5 = new TableColumn<>("Unidad"); c5.setCellValueFactory(d -> d.getValue().unidad);
        TableColumn<ProductoRow, String> c6 = new TableColumn<>("Costo"); c6.setCellValueFactory(d -> d.getValue().costoStr);
        TableColumn<ProductoRow, String> c7 = new TableColumn<>("Stock mín."); c7.setCellValueFactory(d -> d.getValue().stockMinStr);
        TableColumn<ProductoRow, String> c8 = new TableColumn<>("Estado"); c8.setCellValueFactory(d -> d.getValue().estado);
        TableColumn<ProductoRow, String> c9 = new TableColumn<>("Vencimiento"); c9.setCellValueFactory(d -> d.getValue().vencStr);
        TableColumn<ProductoRow, Void> c10 = new TableColumn<>("Acciones");
        c10.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✎"); private final Button btnEnt = new Button("📥"); private final Button btnSal = new Button("📤");
            { String s = "-fx-cursor:hand;-fx-font-size:11px;-fx-padding:3px 8px;"; btnEdit.setStyle(s+"-fx-background-color:#3498db;-fx-text-fill:white;"); btnEnt.setStyle(s+"-fx-background-color:#27ae60;-fx-text-fill:white;"); btnSal.setStyle(s+"-fx-background-color:#e67e22;-fx-text-fill:white;"); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                ProductoRow r = getTableView().getItems().get(getIndex());
                btnEdit.setOnAction(e -> editarProductoPorId(r.id));
                btnEnt.setOnAction(e -> irAEntradaProducto(r.id));
                btnSal.setOnAction(e -> irASalidaProducto(r.id));
                HBox tb = new HBox(4, btnEdit, btnEnt, btnSal); setGraphic(tb);
            }
        });
        c8.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(item);
                if ("Activo".equals(item)) setTextFill(Color.GREEN); else if ("Agotado".equals(item)) setTextFill(Color.RED); else setTextFill(Color.ORANGE);
            }
        });
        tablaProductos.getColumns().setAll(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10);
        tablaProductos.setItems(productosList);
        tablaProductos.setRowFactory(tv -> {
            TableRow<ProductoRow> row = new TableRow<>();
            row.itemProperty().addListener((obs, o, n) -> {
                if (n == null) { row.setStyle(""); return; }
                if ("Agotado".equals(n.estadoRaw) || "Vencido".equals(n.estadoRaw)) row.setStyle("-fx-background-color:#fde8e8;");
                else if ("Stock Bajo".equals(n.estadoRaw)) row.setStyle("-fx-background-color:#fef9e7;");
                else row.setStyle("");
            });
            return row;
        });
    }

    private void cargarDashboard() {
        Object[] res = InventarioDAO.obtenerResumen();
        lblTotalProductos.setText(String.valueOf((int)res[0]));
        lblAgotados.setText(String.valueOf((int)res[1]));
        lblStockBajo.setText(String.valueOf((int)res[2]));
        lblVencidos.setText(String.valueOf((int)res[3]));
        lblValorInventario.setText(String.format("RD$ %,.0f", (double)res[4]));
        cargarProductos();
    }

    @FXML
    private void buscarProductos() {
        String filtro = txtBuscarProducto.getText().trim();
        productosList.clear();
        for (Object[] p : InventarioDAO.listarProductos(filtro.isEmpty() ? null : filtro)) {
            productosList.add(new ProductoRow(p));
        }
    }

    private void cargarProductos() { buscarProductos(); }

    // ═══════════════════════════════════════════════════════════════════
    //  PRODUCTOS (CRUD)
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaProductosCRUD() {
        TableColumn<ProductoRow, String> c1 = new TableColumn<>("Código"); c1.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<ProductoRow, String> c2 = new TableColumn<>("Nombre"); c2.setCellValueFactory(d -> d.getValue().nombre);
        TableColumn<ProductoRow, String> c3 = new TableColumn<>("Categoría"); c3.setCellValueFactory(d -> d.getValue().categoria);
        TableColumn<ProductoRow, String> c4 = new TableColumn<>("Cantidad"); c4.setCellValueFactory(d -> d.getValue().cantidadStr);
        TableColumn<ProductoRow, String> c5 = new TableColumn<>("Unidad"); c5.setCellValueFactory(d -> d.getValue().unidad);
        TableColumn<ProductoRow, String> c6 = new TableColumn<>("Costo"); c6.setCellValueFactory(d -> d.getValue().costoStr);
        TableColumn<ProductoRow, String> c7 = new TableColumn<>("Estado"); c7.setCellValueFactory(d -> d.getValue().estado);
        TableColumn<ProductoRow, String> c8 = new TableColumn<>("Vencimiento"); c8.setCellValueFactory(d -> d.getValue().vencStr);
        tablaProductosCRUD.getColumns().setAll(c1, c2, c3, c4, c5, c6, c7, c8);
        tablaProductosCRUD.setItems(productosList);
        tablaProductosCRUD.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) cargarProductoFormulario(sel.id);
        });
    }

    private void cargarCategoriasCombo() {
        cmbProdCategoria.setItems(FXCollections.observableArrayList(InventarioDAO.listarCategorias()));
    }

    @FXML
    private void guardarProducto() {
        String nombre = txtProdNombre.getText().trim();
        if (nombre.isEmpty()) { Alertas.advertencia("Validación", "El nombre del producto es obligatorio."); return; }
        String cat = cmbProdCategoria.getValue(); if (cat == null) cat = "";
        String uni = cmbProdUnidad.getValue(); if (uni == null) uni = "UN";
        double cant, cost, stk;
        try {
            cant = txtProdCantidad.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtProdCantidad.getText().trim());
            cost = txtProdCosto.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtProdCosto.getText().trim());
            stk = txtProdStockMin.getText().trim().isEmpty() ? 5 : Double.parseDouble(txtProdStockMin.getText().trim());
        } catch (NumberFormatException e) { Alertas.advertencia("Validación", "Cantidad, costo y stock mínimo deben ser números."); return; }
        String est = cmbProdEstado.getValue(); if (est == null) est = "Activo";
        LocalDate venc = dateProdVenc.getValue();
        String obs = txtProdObservacion.getText().trim();
        String codigo = txtProdCodigo.getText().trim();

        boolean ok;
        if (idProductoEditando > 0) {
            ok = InventarioDAO.actualizarProducto(idProductoEditando, nombre, cat, uni, cant, cost, stk, est, venc, obs);
        } else {
            if (codigo.isEmpty()) codigo = InventarioDAO.generarCodigoProducto();
            int id = InventarioDAO.guardarProducto(codigo, nombre, cat, uni, cant, cost, stk, est, venc, obs);
            ok = id > 0;
        }
        if (ok) {
            Alertas.exito("Productos", "Producto guardado correctamente.");
            nuevoProducto();
            cargarProductos();
        } else {
            Alertas.error("Error", "No se pudo guardar el producto.");
        }
    }

    @FXML
    private void nuevoProducto() {
        idProductoEditando = -1;
        txtProdCodigo.setText(InventarioDAO.generarCodigoProducto());
        txtProdNombre.clear(); txtProdCantidad.clear(); txtProdCosto.clear(); txtProdStockMin.setText("5");
        cmbProdCategoria.setValue(null); cmbProdUnidad.setValue("UN"); cmbProdEstado.setValue("Activo");
        dateProdVenc.setValue(null); txtProdObservacion.clear();
    }

    @FXML
    private void limpiarProducto() { nuevoProducto(); }

    private void cargarProductoFormulario(int id) {
        Object[] p = InventarioDAO.obtenerProducto(id);
        if (p == null) return;
        idProductoEditando = id;
        txtProdCodigo.setText((String)p[1]); txtProdNombre.setText((String)p[2]);
        cmbProdCategoria.setValue((String)p[3]); cmbProdUnidad.setValue((String)p[4]);
        txtProdCantidad.setText(String.valueOf((double)p[5])); txtProdCosto.setText(String.format("%.2f", (double)p[6]));
        txtProdStockMin.setText(String.valueOf((double)p[7])); cmbProdEstado.setValue((String)p[8]);
        dateProdVenc.setValue((LocalDate)p[9]); txtProdObservacion.setText((String)p[10]);
        mostrarVista(scrollProductos);
    }

    private void editarProductoPorId(int id) { cargarProductoFormulario(id); }

    @FXML
    private void eliminarProducto() {
        ProductoRow sel = tablaProductosCRUD.getSelectionModel().getSelectedItem();
        if (sel == null) { Alertas.advertencia("Eliminar", "Seleccione un producto de la tabla."); return; }
        if (Alertas.confirmar("Eliminar producto", "¿Seguro de eliminar " + sel.nombre.get() + "?\nSe borrarán también sus movimientos.")) {
            if (InventarioDAO.eliminarProducto(sel.id)) {
                Alertas.exito("Productos", "Producto eliminado.");
                cargarProductos();
                if (idProductoEditando == sel.id) nuevoProducto();
            } else { Alertas.error("Error", "No se pudo eliminar."); }
        }
    }

    @FXML private void actualizarProductos() { cargarProductos(); }

    @FXML private void irAEntradas() { mostrarVista(scrollEntradas); cargarComboProductos(cmbEntradaProducto); cargarTablaMovimientos(tablaEntradas, "Entrada"); }
    @FXML private void irASalidas() { mostrarVista(scrollSalidas); cargarComboProductos(cmbSalidaProducto); cargarTablaMovimientos(tablaSalidas, "Salida"); }

    private void irAEntradaProducto(int idProd) {
        mostrarVista(scrollEntradas);
        cargarComboProductos(cmbEntradaProducto);
        for (ItemCombo it : cmbEntradaProducto.getItems()) { if (it.id == idProd) { cmbEntradaProducto.setValue(it); break; } }
        cargarTablaMovimientos(tablaEntradas, "Entrada");
    }

    private void irASalidaProducto(int idProd) {
        mostrarVista(scrollSalidas);
        cargarComboProductos(cmbSalidaProducto);
        for (ItemCombo it : cmbSalidaProducto.getItems()) { if (it.id == idProd) { cmbSalidaProducto.setValue(it); break; } }
        cargarTablaMovimientos(tablaSalidas, "Salida");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ENTRADAS / SALIDAS
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablasMovimientosLigeras() {
        for (TableView<MovimientoRow> tv : new TableView[]{tablaEntradas, tablaSalidas}) {
            TableColumn<MovimientoRow, String> c1 = new TableColumn<>("Fecha"); c1.setCellValueFactory(d -> d.getValue().fecha);
            TableColumn<MovimientoRow, String> c2 = new TableColumn<>("Producto"); c2.setCellValueFactory(d -> d.getValue().producto);
            TableColumn<MovimientoRow, String> c3 = new TableColumn<>("Cantidad"); c3.setCellValueFactory(d -> d.getValue().cantidad);
            TableColumn<MovimientoRow, String> c4 = new TableColumn<>("Observación"); c4.setCellValueFactory(d -> d.getValue().observacion);
            TableColumn<MovimientoRow, String> c5 = new TableColumn<>("Usuario"); c5.setCellValueFactory(d -> d.getValue().usuario);
            tv.getColumns().setAll(c1, c2, c3, c4, c5);
        }
    }

    private void cargarComboProductos(ComboBox<ItemCombo> combo) {
        combo.setItems(FXCollections.observableArrayList());
        for (Object[] p : InventarioDAO.listarTodos()) {
            combo.getItems().add(new ItemCombo((int)p[0], (String)p[2] + " (" + (String)p[1] + ")", ""));
        }
    }

    private void cargarTablaMovimientos(TableView<MovimientoRow> tabla, String tipo) {
        ObservableList<MovimientoRow> items = FXCollections.observableArrayList();
        for (Object[] m : InventarioDAO.listarMovimientos(null)) {
            if (m[1].equals(tipo)) items.add(new MovimientoRow(m[5] != null ? m[5].toString() : "", (String)m[3], String.valueOf(m[4]), (String)m[6], (String)m[7]));
        }
        tabla.setItems(items);
    }

    @FXML
    private void registrarEntrada() {
        registrarMovimiento("Entrada", cmbEntradaProducto, txtEntradaCantidad, dateEntrada, txtEntradaObservacion);
    }

    @FXML
    private void registrarSalida() {
        registrarMovimiento("Salida", cmbSalidaProducto, txtSalidaCantidad, dateSalida, txtSalidaObservacion);
    }

    private void registrarMovimiento(String tipo, ComboBox<ItemCombo> combo, TextField cantField, DatePicker date, TextField obsField) {
        ItemCombo item = combo.getValue();
        if (item == null) { Alertas.advertencia("Validación", "Seleccione un producto."); return; }
        String cantStr = cantField.getText().trim();
        if (cantStr.isEmpty()) { Alertas.advertencia("Validación", "Ingrese la cantidad."); return; }
        double cant;
        try { cant = Double.parseDouble(cantStr); if (cant <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { Alertas.advertencia("Validación", "Cantidad debe ser un número positivo."); return; }
        LocalDate fecha = date.getValue();
        if (fecha == null) fecha = LocalDate.now();
        String obs = obsField.getText().trim();

        if ("Salida".equals(tipo)) {
            Object[] prod = InventarioDAO.obtenerProducto(item.id);
            if (prod != null && (double)prod[5] < cant) {
                if (!Alertas.confirmar("Stock insuficiente", "El producto tiene solo " + String.format("%.2f", (double)prod[5]) + " unidades. ¿Desea registrar la salida de todas formas?")) return;
            }
        }

        if (InventarioDAO.registrarMovimiento(tipo, item.id, cant, fecha, obs, usuarioActual)) {
            Alertas.exito(tipo, tipo + " registrada correctamente.\nProducto: " + item.nombre + "\nCantidad: " + cantStr);
            cantField.clear(); obsField.clear(); combo.setValue(null);
            date.setValue(LocalDate.now());
            cargarTablaMovimientos(tipo.equals("Entrada") ? tablaEntradas : tablaSalidas, tipo);
            cargarDashboard();
        } else {
            Alertas.error("Error", "No se pudo registrar la " + tipo.toLowerCase() + ".");
        }
    }

    @FXML private void limpiarEntrada() { cmbEntradaProducto.setValue(null); txtEntradaCantidad.clear(); txtEntradaObservacion.clear(); dateEntrada.setValue(LocalDate.now()); lblEntradaInfo.setText(""); }
    @FXML private void limpiarSalida() { cmbSalidaProducto.setValue(null); txtSalidaCantidad.clear(); txtSalidaObservacion.clear(); dateSalida.setValue(LocalDate.now()); lblSalidaInfo.setText(""); }

    // ═══════════════════════════════════════════════════════════════════
    //  MOVIMIENTOS
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaMovimientos() {
        TableColumn<MovimientoFullRow, Integer> c1 = new TableColumn<>("ID"); c1.setCellValueFactory(d -> d.getValue().id.asObject());
        TableColumn<MovimientoFullRow, String> c2 = new TableColumn<>("Tipo"); c2.setCellValueFactory(d -> d.getValue().tipo);
        c2.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText(item);
                if ("Entrada".equals(item)) { setStyle("-fx-text-fill:#27ae60;"); } else { setStyle("-fx-text-fill:#e67e22;"); }
            }
        });
        TableColumn<MovimientoFullRow, String> c3 = new TableColumn<>("Código"); c3.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<MovimientoFullRow, String> c4 = new TableColumn<>("Producto"); c4.setCellValueFactory(d -> d.getValue().producto);
        TableColumn<MovimientoFullRow, String> c5 = new TableColumn<>("Cantidad"); c5.setCellValueFactory(d -> d.getValue().cantidad);
        TableColumn<MovimientoFullRow, String> c6 = new TableColumn<>("Fecha"); c6.setCellValueFactory(d -> d.getValue().fecha);
        TableColumn<MovimientoFullRow, String> c7 = new TableColumn<>("Observación"); c7.setCellValueFactory(d -> d.getValue().observacion);
        TableColumn<MovimientoFullRow, String> c8 = new TableColumn<>("Usuario"); c8.setCellValueFactory(d -> d.getValue().usuario);
        tablaMovimientos.getColumns().setAll(c1, c2, c3, c4, c5, c6, c7, c8);
        tablaMovimientos.setItems(movimientosList);
    }

    private void cargarMovimientos(String filtro) {
        movimientosList.clear();
        for (Object[] m : InventarioDAO.listarMovimientos(filtro)) {
            movimientosList.add(new MovimientoFullRow((int)m[0], (String)m[1], (String)m[2], (String)m[3], String.valueOf(m[4]), m[5] != null ? m[5].toString() : "", (String)m[6], (String)m[7]));
        }
    }

    @FXML private void buscarMovimientos() { cargarMovimientos(txtBuscarMovimiento.getText().trim().isEmpty() ? null : txtBuscarMovimiento.getText().trim()); }
    @FXML private void actualizarMovimientos() { txtBuscarMovimiento.clear(); cargarMovimientos(null); }

    // ═══════════════════════════════════════════════════════════════════
    //  ALERTAS
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaAlertas() {
        TableColumn<StockBajoRow, String> s1 = new TableColumn<>("Código"); s1.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<StockBajoRow, String> s2 = new TableColumn<>("Producto"); s2.setCellValueFactory(d -> d.getValue().producto);
        TableColumn<StockBajoRow, String> s3 = new TableColumn<>("Unidad"); s3.setCellValueFactory(d -> d.getValue().unidad);
        TableColumn<StockBajoRow, String> s4 = new TableColumn<>("Stock actual"); s4.setCellValueFactory(d -> d.getValue().stockActual);
        TableColumn<StockBajoRow, String> s5 = new TableColumn<>("Stock mínimo"); s5.setCellValueFactory(d -> d.getValue().stockMin);
        TableColumn<StockBajoRow, String> s6 = new TableColumn<>("Faltante"); s6.setCellValueFactory(d -> d.getValue().faltante);
        tablaStockBajo.getColumns().setAll(s1, s2, s3, s4, s5, s6);

        TableColumn<VencidoRow, String> v1 = new TableColumn<>("Código"); v1.setCellValueFactory(d -> d.getValue().codigo);
        TableColumn<VencidoRow, String> v2 = new TableColumn<>("Producto"); v2.setCellValueFactory(d -> d.getValue().producto);
        TableColumn<VencidoRow, String> v3 = new TableColumn<>("Categoría"); v3.setCellValueFactory(d -> d.getValue().categoria);
        TableColumn<VencidoRow, String> v4 = new TableColumn<>("Cantidad"); v4.setCellValueFactory(d -> d.getValue().cantidad);
        TableColumn<VencidoRow, String> v5 = new TableColumn<>("Vence"); v5.setCellValueFactory(d -> d.getValue().fechaVence);
        TableColumn<VencidoRow, String> v6 = new TableColumn<>("Días vencido"); v6.setCellValueFactory(d -> d.getValue().diasVencido);
        v6.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                setText(item);
                try { int d = Integer.parseInt(item.replace(" días", "")); if (d > 30) setTextFill(Color.RED); else if (d > 7) setTextFill(Color.ORANGE); else setTextFill(Color.DARKRED); } catch (Exception ignored) {}
            }
        });
        tablaVencidos.getColumns().setAll(v1, v2, v3, v4, v5, v6);
    }

    private void cargarAlertas() {
        List<Object[]> bajos = InventarioDAO.listarStockBajo();
        ObservableList<StockBajoRow> itemsBajo = FXCollections.observableArrayList();
        double valorRiesgo = 0;
        for (Object[] b : bajos) {
            double cant = (double)b[4], min = (double)b[5];
            itemsBajo.add(new StockBajoRow((String)b[1], (String)b[2], (String)b[3], String.format("%.2f", cant), String.format("%.2f", min), String.format("%.2f", min - cant)));
        }
        lblAlertStockBajo.setText(String.valueOf(bajos.size()));
        tablaStockBajo.setItems(itemsBajo);

        List<Object[]> venc = InventarioDAO.listarVencidos();
        ObservableList<VencidoRow> itemsVenc = FXCollections.observableArrayList();
        for (Object[] v : venc) {
            LocalDate fv = (LocalDate)v[5];
            long dias = ChronoUnit.DAYS.between(fv, LocalDate.now());
            itemsVenc.add(new VencidoRow((String)v[1], (String)v[2], (String)v[3], String.valueOf((double)v[4]), fv.toString(), dias + " días"));
        }
        lblAlertVencidos.setText(String.valueOf(venc.size()));
        lblAlertValorRiesgo.setText(String.format("RD$ %,.0f", valorRiesgo));
        tablaVencidos.setItems(itemsVenc);
    }

    @FXML private void actualizarAlertas() { cargarAlertas(); cargarDashboard(); }

    // ═══════════════════════════════════════════════════════════════════
    //  CATEGORÍAS
    // ═══════════════════════════════════════════════════════════════════

    private void configurarTablaCategorias() {
        TableColumn<CategoriaRow, Integer> c1 = new TableColumn<>("ID"); c1.setCellValueFactory(d -> d.getValue().id.asObject());
        TableColumn<CategoriaRow, String> c2 = new TableColumn<>("Nombre"); c2.setCellValueFactory(d -> d.getValue().nombre);
        TableColumn<CategoriaRow, String> c3 = new TableColumn<>("Descripción"); c3.setCellValueFactory(d -> d.getValue().descripcion);
        TableColumn<CategoriaRow, Integer> c4 = new TableColumn<>("Productos"); c4.setCellValueFactory(d -> d.getValue().totalProd.asObject());
        tablaCategorias.getColumns().setAll(c1, c2, c3, c4);
        tablaCategorias.setItems(categoriasList);
    }

    private void cargarCategorias() {
        categoriasList.clear();
        for (Object[] c : InventarioDAO.listarCategoriasFull()) {
            categoriasList.add(new CategoriaRow((int)c[0], (String)c[1], (String)c[2], (int)c[3]));
        }
    }

    @FXML
    private void guardarCategoria() {
        String nombre = txtCatNombre.getText().trim();
        if (nombre.isEmpty()) { Alertas.advertencia("Validación", "El nombre de la categoría es obligatorio."); return; }
        if (InventarioDAO.guardarCategoria(nombre, txtCatDescripcion.getText().trim())) {
            Alertas.exito("Categorías", "Categoría guardada.");
            limpiarCategoria();
            cargarCategorias();
            cmbProdCategoria.setItems(FXCollections.observableArrayList(InventarioDAO.listarCategorias()));
        } else { Alertas.error("Error", "No se pudo guardar. Puede que ya exista."); }
    }

    @FXML private void limpiarCategoria() { txtCatNombre.clear(); txtCatDescripcion.clear(); }

    @FXML
    private void eliminarCategoria() {
        CategoriaRow sel = tablaCategorias.getSelectionModel().getSelectedItem();
        if (sel == null) { Alertas.advertencia("Eliminar", "Seleccione una categoría."); return; }
        if (Alertas.confirmar("Eliminar categoría", "¿Seguro de eliminar '" + sel.nombre.get() + "'?")) {
            if (InventarioDAO.eliminarCategoria(sel.nombre.get())) {
                Alertas.exito("Categorías", "Categoría eliminada.");
                cargarCategorias();
                cmbProdCategoria.setItems(FXCollections.observableArrayList(InventarioDAO.listarCategorias()));
            } else { Alertas.error("Error", "No se pudo eliminar."); }
        }
    }

    @FXML private void actualizarCategorias() { cargarCategorias(); }

    // ── NAVEGACIÓN ──────────────────────────────────────────────────────

    @FXML private void volverMenu(ActionEvent event) { Navegacion.volverCentroSistema(event); }
    @FXML private void salirSistema(ActionEvent event) { System.exit(0); }
    @FXML private void exportarReporte() { Alertas.informacion("Exportar", "Funcionalidad de exportación en desarrollo."); }

    // ═══════════════════════════════════════════════════════════════════
    //  CLASES INTERNAS
    // ═══════════════════════════════════════════════════════════════════

    public static class ItemCombo {
        public final int id; public final String nombre, dato;
        public ItemCombo(int id, String nombre, String dato) { this.id = id; this.nombre = nombre; this.dato = dato; }
        @Override public String toString() { return nombre; }
    }

    public static class ProductoRow {
        public final int id;
        public final SimpleStringProperty codigo, nombre, categoria, cantidadStr, unidad, costoStr, stockMinStr, estado, vencStr;
        public final String estadoRaw;
        public ProductoRow(Object[] p) {
            id = (int)p[0];
            codigo = new SimpleStringProperty((String)p[1]);
            nombre = new SimpleStringProperty((String)p[2]);
            categoria = new SimpleStringProperty((String)p[3]);
            unidad = new SimpleStringProperty((String)p[4]);
            double cant = (double)p[5];
            cantidadStr = new SimpleStringProperty(String.format("%.2f", cant));
            costoStr = new SimpleStringProperty(String.format("%.2f", (double)p[6]));
            stockMinStr = new SimpleStringProperty(String.valueOf((double)p[7]));
            String est = (String)p[8];
            LocalDate venc = (LocalDate)p[9];
            if (venc != null && venc.isBefore(LocalDate.now())) { estadoRaw = "Vencido"; est = "⚠ Vencido"; }
            else if ("Activo".equals(est) && cant <= 0) { estadoRaw = "Agotado"; est = "🔴 Agotado"; }
            else if ("Activo".equals(est) && cant <= (double)p[7]) { estadoRaw = "Stock Bajo"; est = "🟡 Stock Bajo"; }
            else { estadoRaw = est; }
            this.estado = new SimpleStringProperty(est);
            vencStr = new SimpleStringProperty(venc != null ? venc.toString() : "---");
        }
    }

    public static class MovimientoRow {
        public final SimpleStringProperty fecha, producto, cantidad, observacion, usuario;
        public MovimientoRow(String f, String p, String c, String o, String u) {
            fecha = new SimpleStringProperty(f); producto = new SimpleStringProperty(p);
            cantidad = new SimpleStringProperty(c); observacion = new SimpleStringProperty(o); usuario = new SimpleStringProperty(u);
        }
    }

    public static class MovimientoFullRow {
        public final SimpleIntegerProperty id;
        public final SimpleStringProperty tipo, codigo, producto, cantidad, fecha, observacion, usuario;
        public MovimientoFullRow(int id, String t, String c, String p, String cant, String f, String o, String u) {
            this.id = new SimpleIntegerProperty(id); tipo = new SimpleStringProperty(t); codigo = new SimpleStringProperty(c);
            producto = new SimpleStringProperty(p); cantidad = new SimpleStringProperty(cant); fecha = new SimpleStringProperty(f);
            observacion = new SimpleStringProperty(o); usuario = new SimpleStringProperty(u);
        }
    }

    public static class StockBajoRow {
        public final SimpleStringProperty codigo, producto, unidad, stockActual, stockMin, faltante;
        public StockBajoRow(String cod, String prod, String uni, String act, String min, String fal) {
            codigo = new SimpleStringProperty(cod); producto = new SimpleStringProperty(prod);
            unidad = new SimpleStringProperty(uni); stockActual = new SimpleStringProperty(act);
            stockMin = new SimpleStringProperty(min); faltante = new SimpleStringProperty(fal);
        }
    }

    public static class VencidoRow {
        public final SimpleStringProperty codigo, producto, categoria, cantidad, fechaVence, diasVencido;
        public VencidoRow(String cod, String prod, String cat, String cant, String fv, String dias) {
            codigo = new SimpleStringProperty(cod); producto = new SimpleStringProperty(prod);
            categoria = new SimpleStringProperty(cat); cantidad = new SimpleStringProperty(cant);
            fechaVence = new SimpleStringProperty(fv); diasVencido = new SimpleStringProperty(dias);
        }
    }

    public static class CategoriaRow {
        public final SimpleIntegerProperty id, totalProd;
        public final SimpleStringProperty nombre, descripcion;
        public CategoriaRow(int id, String nom, String desc, int total) {
            this.id = new SimpleIntegerProperty(id); nombre = new SimpleStringProperty(nom);
            descripcion = new SimpleStringProperty(desc); totalProd = new SimpleIntegerProperty(total);
        }
    }
}
