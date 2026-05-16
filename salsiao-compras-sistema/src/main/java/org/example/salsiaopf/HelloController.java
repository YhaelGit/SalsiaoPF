package org.example.salsiaopf;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private ImageView logoImage;

    // Toggle buttons
    @FXML private ToggleButton segOrder;
    @FXML private ToggleButton segSupplier;
    @FXML private ToggleButton segReception;
    @FXML private ToggleButton segIngredient;
    @FXML private ToggleButton segPayment;

    // Vistas
    @FXML private VBox viewOrder;
    @FXML private VBox viewSupplier;
    @FXML private VBox viewReception;
    @FXML private VBox viewIngredient;
    @FXML private VBox viewPayment;

    // === ORDEN DE COMPRA ===
    @FXML private TextField txtOrdenId, txtOrdenNumero, txtOrdenNombre, txtOrdenTipoProd;
    @FXML private DatePicker dpOrdenFecha;
    @FXML private ComboBox<Proveedor> cbOrdenProveedor;
    @FXML private TextField txtBuscarOrden;
    @FXML private TableView<OrdenCompra> tablaOrdenes;
    @FXML private TableColumn<OrdenCompra, Integer> colOrdenId;
    @FXML private TableColumn<OrdenCompra, String> colOrdenNumero, colOrdenNombre, colOrdenEstado;

    // === PROVEEDOR ===
    @FXML private TextField txtProvId, txtProvCodigo, txtProvNombre, txtProvTelefono, txtProvTipoProd, txtProvCantidad;
    @FXML private TextField txtBuscarProv;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colProvId;
    @FXML private TableColumn<Proveedor, String> colProvCodigo, colProvNombre, colProvTelefono, colProvEstado;

    // === RECEPCION ===
    @FXML private TextField txtRecepId, txtRecepNumeroOrden, txtRecepResponsable, txtRecepCantidad;
    @FXML private DatePicker dpRecepFecha;
    @FXML private ComboBox<String> cbRecepEstadoProd;
    @FXML private ComboBox<Proveedor> cbRecepProveedor;
    @FXML private TextField txtBuscarRecep;
    @FXML private TableView<Recepcion> tablaRecepciones;
    @FXML private TableColumn<Recepcion, Integer> colRecepId;
    @FXML private TableColumn<Recepcion, String> colRecepOrden, colRecepEstado, colRecepResponsable;

    // === INGREDIENTE ===
    @FXML private TextField txtIngId, txtIngCodigo, txtIngNombre, txtIngCategoria, txtIngUnidad, txtIngPrecio, txtIngNotas, txtIngCantidad;
    @FXML private DatePicker dpIngVencimiento;
    @FXML private ComboBox<Proveedor> cbIngProveedor;
    @FXML private TextField txtBuscarIng;
    @FXML private TableView<Ingrediente> tablaIngredientes;
    @FXML private TableColumn<Ingrediente, Integer> colIngId;
    @FXML private TableColumn<Ingrediente, String> colIngCodigo, colIngNombre, colIngCategoria, colIngUnidad;

    // === PAGO ===
    @FXML private TextField txtPagoId, txtPagoMetodo, txtPagoMonto, txtPagoItbis, txtPagoReferencia;
    @FXML private DatePicker dpPagoFecha;
    @FXML private TextField txtBuscarPago;
    @FXML private TableView<Pago> tablaPagos;
    @FXML private TableColumn<Pago, Integer> colPagoId;
    @FXML private TableColumn<Pago, String> colPagoMetodo, colPagoReferencia;
    @FXML private TableColumn<Pago, Double> colPagoMonto;

    private ObservableList<OrdenCompra> listaOrdenes = FXCollections.observableArrayList();
    private ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
    private ObservableList<Recepcion> listaRecepciones = FXCollections.observableArrayList();
    private ObservableList<Ingrediente> listaIngredientes = FXCollections.observableArrayList();
    private ObservableList<Pago> listaPagos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        configurarTablas();
        cargarCombos();
        iniciarVista();
    }

    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
                if (stream != null) {
                    logoImage.setImage(new Image(stream));
                    logoImage.setClip(new Circle(39, 39, 39));
                }
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void configurarTablas() {
        // Ordenes
        colOrdenId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrdenNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colOrdenNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colOrdenEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaOrdenes.setItems(listaOrdenes);
        tablaOrdenes.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposOrden(nuevo);
        });

        // Proveedores
        colProvId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProvCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProvNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProvTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colProvEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaProveedores.setItems(listaProveedores);
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposProveedor(nuevo);
        });

        // Recepciones
        colRecepId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRecepOrden.setCellValueFactory(new PropertyValueFactory<>("numeroOrden"));
        colRecepEstado.setCellValueFactory(new PropertyValueFactory<>("estadoProducto"));
        colRecepResponsable.setCellValueFactory(new PropertyValueFactory<>("responsable"));
        tablaRecepciones.setItems(listaRecepciones);
        tablaRecepciones.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposRecepcion(nuevo);
        });

        // Ingredientes
        colIngId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIngCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colIngNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colIngCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colIngUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
        tablaIngredientes.setItems(listaIngredientes);
        tablaIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposIngrediente(nuevo);
        });

        // Pagos
        colPagoId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPagoMetodo.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colPagoMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colPagoReferencia.setCellValueFactory(new PropertyValueFactory<>("referencia"));
        tablaPagos.setItems(listaPagos);

        cargarTodosLosDatos();
    }

    private void cargarCombos() {
        cbRecepEstadoProd.setItems(FXCollections.observableArrayList("Conforme", "Dañado", "Incompleto"));
        cargarProveedoresCombo();
    }

    private void cargarProveedoresCombo() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT id_proveedor, codigo, nombre FROM Proveedores WHERE estado='Activo'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ObservableList<Proveedor> provs = FXCollections.observableArrayList();
            while (rs.next()) {
                provs.add(new Proveedor(rs.getInt("id_proveedor"), rs.getString("codigo"),
                    rs.getString("nombre"), "", "", "Activo"));
            }
            cbOrdenProveedor.setItems(provs);
            cbRecepProveedor.setItems(provs);
            cbIngProveedor.setItems(provs);
        } catch (SQLException e) {
            System.out.println("Error cargando proveedores: " + e.getMessage());
        }
    }

    private void cargarTodosLosDatos() {
        cargarOrdenes();
        cargarProveedores();
        cargarRecepciones();
        cargarIngredientes();
        cargarPagos();
    }

    private void iniciarVista() {
        if (segOrder != null) segOrder.setSelected(true);
        showOnly(viewOrder);
    }

    private void showOnly(VBox target) {
        VBox[] all = { viewOrder, viewSupplier, viewReception, viewIngredient, viewPayment };
        for (VBox v : all) {
            if (v != null) {
                v.setVisible(v == target);
                v.setManaged(v == target);
            }
        }
    }

    @FXML private void showOrder() { showOnly(viewOrder); }
    @FXML private void showSupplier() { showOnly(viewSupplier); cargarProveedoresCombo(); }
    @FXML private void showReception() { showOnly(viewReception); }
    @FXML private void showIngredient() { showOnly(viewIngredient); cargarProveedoresCombo(); }
    @FXML private void showPayment() { showOnly(viewPayment); }

    // ================= ORDEN DE COMPRA CRUD =================
    private void llenarCamposOrden(OrdenCompra o) {
        txtOrdenId.setText(String.valueOf(o.getId()));
        txtOrdenNumero.setText(o.getNumero());
        txtOrdenNombre.setText(o.getNombre());
        txtOrdenTipoProd.setText(o.getTipoProductos());
        dpOrdenFecha.setValue(o.getFecha() != null ? java.time.LocalDate.parse(o.getFecha()) : null);
    }

    @FXML
    private void guardarOrden() {
        if (txtOrdenNumero.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El número de orden es obligatorio", Alert.AlertType.WARNING);
            return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtOrdenId.getText().trim();
            String sql;
            PreparedStatement ps;
            if (idTxt.isEmpty()) {
                sql = "INSERT INTO OrdenesCompra (numero_orden, fecha, id_proveedor, nombre, tipo_productos, estado) VALUES (?,?,?,?,?,'Pendiente')";
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                sql = "UPDATE OrdenesCompra SET numero_orden=?, fecha=?, id_proveedor=?, nombre=?, tipo_productos=? WHERE id_orden=?";
                ps = conn.prepareStatement(sql);
            }
            ps.setString(1, txtOrdenNumero.getText());
            ps.setDate(2, dpOrdenFecha.getValue() != null ? Date.valueOf(dpOrdenFecha.getValue()) : null);
            Proveedor p = cbOrdenProveedor.getValue();
            ps.setObject(3, p != null ? p.getId() : null);
            ps.setString(4, txtOrdenNombre.getText());
            ps.setString(5, txtOrdenTipoProd.getText());
            if (!idTxt.isEmpty()) ps.setInt(6, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Orden guardada correctamente", Alert.AlertType.INFORMATION);
            limpiarOrden();
            cargarOrdenes();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al guardar orden: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarOrden() {
        eliminarRegistro("OrdenesCompra", "id_orden", txtOrdenId.getText(), this::cargarOrdenes);
    }

    @FXML
    private void limpiarOrden() {
        txtOrdenId.clear(); txtOrdenNumero.clear(); txtOrdenNombre.clear(); txtOrdenTipoProd.clear();
        dpOrdenFecha.setValue(null); cbOrdenProveedor.setValue(null);
    }

    @FXML
    private void buscarOrden() {
        buscarEnTabla("OrdenesCompra", "numero_orden", txtBuscarOrden.getText(),
            rs -> new OrdenCompra(rs.getInt("id_orden"), rs.getString("numero_orden"),
                rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "",
                rs.getString("nombre"), rs.getString("tipo_productos"), rs.getString("estado")),
            listaOrdenes);
    }

    private void cargarOrdenes() {
        cargarTabla("SELECT * FROM OrdenesCompra ORDER BY id_orden DESC",
            rs -> new OrdenCompra(rs.getInt("id_orden"), rs.getString("numero_orden"),
                rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "",
                rs.getString("nombre"), rs.getString("tipo_productos"), rs.getString("estado")),
            listaOrdenes);
    }

    // ================= PROVEEDOR CRUD =================
    private void llenarCamposProveedor(Proveedor p) {
        txtProvId.setText(String.valueOf(p.getId()));
        txtProvCodigo.setText(p.getCodigo());
        txtProvNombre.setText(p.getNombre());
        txtProvTelefono.setText(p.getTelefono());
        txtProvTipoProd.setText(p.getTipoProductos());
    }

    @FXML
    private void guardarProveedor() {
        if (txtProvNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING); return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtProvId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Proveedores (codigo, nombre, telefono, tipo_productos, estado) VALUES (?,?,?,?,'Activo')"
                : "UPDATE Proveedores SET codigo=?, nombre=?, telefono=?, tipo_productos=? WHERE id_proveedor=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtProvCodigo.getText());
            ps.setString(2, txtProvNombre.getText());
            ps.setString(3, txtProvTelefono.getText());
            ps.setString(4, txtProvTipoProd.getText());
            if (!idTxt.isEmpty()) ps.setInt(5, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Proveedor guardado", Alert.AlertType.INFORMATION);
            limpiarProveedor(); cargarProveedores(); cargarProveedoresCombo();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarProveedor() {
        eliminarRegistro("Proveedores", "id_proveedor", txtProvId.getText(), () -> { cargarProveedores(); cargarProveedoresCombo(); });
    }

    @FXML
    private void limpiarProveedor() {
        txtProvId.clear(); txtProvCodigo.clear(); txtProvNombre.clear(); txtProvTelefono.clear(); txtProvTipoProd.clear(); txtProvCantidad.clear();
    }

    @FXML
    private void buscarProveedor() {
        buscarEnTabla("Proveedores", "nombre", txtBuscarProv.getText(),
            rs -> new Proveedor(rs.getInt("id_proveedor"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("telefono"),
                rs.getString("tipo_productos"), rs.getString("estado")),
            listaProveedores);
    }

    private void cargarProveedores() {
        cargarTabla("SELECT * FROM Proveedores ORDER BY nombre",
            rs -> new Proveedor(rs.getInt("id_proveedor"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("telefono"),
                rs.getString("tipo_productos"), rs.getString("estado")),
            listaProveedores);
    }

    // ================= RECEPCION CRUD =================
    private void llenarCamposRecepcion(Recepcion r) {
        txtRecepId.setText(String.valueOf(r.getId()));
        txtRecepNumeroOrden.setText(r.getNumeroOrden());
        txtRecepResponsable.setText(r.getResponsable());
        txtRecepCantidad.setText(String.valueOf(r.getCantidadRecibida()));
        cbRecepEstadoProd.setValue(r.getEstadoProducto());
    }

    @FXML
    private void guardarRecepcion() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtRecepId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Recepciones (numero_orden, id_proveedor, fecha_recepcion, responsable, estado_producto, cantidad_recibida) VALUES (?,?,?,?,?,?)"
                : "UPDATE Recepciones SET numero_orden=?, id_proveedor=?, fecha_recepcion=?, responsable=?, estado_producto=?, cantidad_recibida=? WHERE id_recepcion=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtRecepNumeroOrden.getText());
            Proveedor p = cbRecepProveedor.getValue();
            ps.setObject(2, p != null ? p.getId() : null);
            ps.setDate(3, dpRecepFecha.getValue() != null ? Date.valueOf(dpRecepFecha.getValue()) : null);
            ps.setString(4, txtRecepResponsable.getText());
            ps.setString(5, cbRecepEstadoProd.getValue());
            ps.setString(6, txtRecepCantidad.getText());
            if (!idTxt.isEmpty()) ps.setInt(7, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Recepción guardada", Alert.AlertType.INFORMATION);
            limpiarRecepcion(); cargarRecepciones();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarRecepcion() {
        eliminarRegistro("Recepciones", "id_recepcion", txtRecepId.getText(), this::cargarRecepciones);
    }

    @FXML
    private void limpiarRecepcion() {
        txtRecepId.clear(); txtRecepNumeroOrden.clear(); txtRecepResponsable.clear(); txtRecepCantidad.clear();
        dpRecepFecha.setValue(null); cbRecepProveedor.setValue(null); cbRecepEstadoProd.setValue(null);
    }

    @FXML
    private void buscarRecepcion() {
        buscarEnTabla("Recepciones", "numero_orden", txtBuscarRecep.getText(),
            rs -> new Recepcion(rs.getInt("id_recepcion"), rs.getString("numero_orden"),
                rs.getString("responsable"), rs.getString("estado_producto"),
                rs.getDouble("cantidad_recibida")),
            listaRecepciones);
    }

    private void cargarRecepciones() {
        cargarTabla("SELECT * FROM Recepciones ORDER BY fecha_recepcion DESC",
            rs -> new Recepcion(rs.getInt("id_recepcion"), rs.getString("numero_orden"),
                rs.getString("responsable"), rs.getString("estado_producto"),
                rs.getDouble("cantidad_recibida")),
            listaRecepciones);
    }

    // ================= INGREDIENTE CRUD =================
    private void llenarCamposIngrediente(Ingrediente i) {
        txtIngId.setText(String.valueOf(i.getId()));
        txtIngCodigo.setText(i.getCodigo());
        txtIngNombre.setText(i.getNombre());
        txtIngCategoria.setText(i.getCategoria());
        txtIngUnidad.setText(i.getUnidadMedida());
        txtIngPrecio.setText(String.valueOf(i.getPrecioUnitario()));
        txtIngNotas.setText(i.getNotas());
        dpIngVencimiento.setValue(i.getFechaVencimiento() != null ? java.time.LocalDate.parse(i.getFechaVencimiento()) : null);
    }

    @FXML
    private void guardarIngrediente() {
        if (txtIngNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING); return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtIngId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Ingredientes (codigo, nombre, categoria, unidad_medida, precio_unitario, id_proveedor, fecha_vencimiento, notas, estado) VALUES (?,?,?,?,?,?,?,?,'Activo')"
                : "UPDATE Ingredientes SET codigo=?, nombre=?, categoria=?, unidad_medida=?, precio_unitario=?, id_proveedor=?, fecha_vencimiento=?, notas=? WHERE id_ingrediente=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtIngCodigo.getText());
            ps.setString(2, txtIngNombre.getText());
            ps.setString(3, txtIngCategoria.getText());
            ps.setString(4, txtIngUnidad.getText());
            ps.setString(5, txtIngPrecio.getText());
            Proveedor p = cbIngProveedor.getValue();
            ps.setObject(6, p != null ? p.getId() : null);
            ps.setDate(7, dpIngVencimiento.getValue() != null ? Date.valueOf(dpIngVencimiento.getValue()) : null);
            ps.setString(8, txtIngNotas.getText());
            if (!idTxt.isEmpty()) ps.setInt(9, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Ingrediente guardado", Alert.AlertType.INFORMATION);
            limpiarIngrediente(); cargarIngredientes();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarIngrediente() {
        eliminarRegistro("Ingredientes", "id_ingrediente", txtIngId.getText(), this::cargarIngredientes);
    }

    @FXML
    private void limpiarIngrediente() {
        txtIngId.clear(); txtIngCodigo.clear(); txtIngNombre.clear(); txtIngCategoria.clear();
        txtIngUnidad.clear(); txtIngPrecio.clear(); txtIngCantidad.clear(); txtIngNotas.clear();
        dpIngVencimiento.setValue(null); cbIngProveedor.setValue(null);
    }

    @FXML
    private void buscarIngrediente() {
        buscarEnTabla("Ingredientes", "nombre", txtBuscarIng.getText(),
            rs -> new Ingrediente(rs.getInt("id_ingrediente"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("categoria"),
                rs.getString("unidad_medida"), rs.getDouble("precio_unitario"),
                rs.getDate("fecha_vencimiento") != null ? rs.getDate("fecha_vencimiento").toString() : null,
                rs.getString("notas")),
            listaIngredientes);
    }

    private void cargarIngredientes() {
        cargarTabla("SELECT * FROM Ingredientes ORDER BY nombre",
            rs -> new Ingrediente(rs.getInt("id_ingrediente"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getString("categoria"),
                rs.getString("unidad_medida"), rs.getDouble("precio_unitario"),
                rs.getDate("fecha_vencimiento") != null ? rs.getDate("fecha_vencimiento").toString() : null,
                rs.getString("notas")),
            listaIngredientes);
    }

    // ================= PAGO CRUD =================
    @FXML
    private void guardarPago() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtPagoId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Pagos (metodo_pago, fecha_pago, monto, itbis, referencia) VALUES (?,?,?,?,?)"
                : "UPDATE Pagos SET metodo_pago=?, fecha_pago=?, monto=?, itbis=?, referencia=? WHERE id_pago=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtPagoMetodo.getText());
            ps.setDate(2, dpPagoFecha.getValue() != null ? Date.valueOf(dpPagoFecha.getValue()) : null);
            ps.setString(3, txtPagoMonto.getText());
            ps.setString(4, txtPagoItbis.getText());
            ps.setString(5, txtPagoReferencia.getText());
            if (!idTxt.isEmpty()) ps.setInt(6, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Pago guardado", Alert.AlertType.INFORMATION);
            limpiarPago(); cargarPagos();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarPago() {
        eliminarRegistro("Pagos", "id_pago", txtPagoId.getText(), this::cargarPagos);
    }

    @FXML
    private void limpiarPago() {
        txtPagoId.clear(); txtPagoMetodo.clear(); txtPagoMonto.clear(); txtPagoItbis.clear(); txtPagoReferencia.clear();
        dpPagoFecha.setValue(null);
    }

    @FXML
    private void buscarPago() {
        buscarEnTabla("Pagos", "referencia", txtBuscarPago.getText(),
            rs -> new Pago(rs.getInt("id_pago"), rs.getString("metodo_pago"),
                rs.getDouble("monto"), rs.getString("referencia")),
            listaPagos);
    }

    private void cargarPagos() {
        cargarTabla("SELECT * FROM Pagos ORDER BY fecha_pago DESC",
            rs -> new Pago(rs.getInt("id_pago"), rs.getString("metodo_pago"),
                rs.getDouble("monto"), rs.getString("referencia")),
            listaPagos);
    }

    // ================= UTILIDADES =================
    private <T> void cargarTabla(String sql, ResultSetMapper<T> mapper, ObservableList<T> lista) {
        lista.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) lista.add(mapper.map(rs));
        } catch (SQLException e) {
            System.out.println("Error cargando datos: " + e.getMessage());
        }
    }

    private <T> void buscarEnTabla(String tabla, String campo, String criterio, ResultSetMapper<T> mapper, ObservableList<T> lista) {
        if (criterio.isEmpty()) { cargarTabla("SELECT * FROM " + tabla, mapper, lista); return; }
        lista.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tabla + " WHERE " + campo + " LIKE ?");
            ps.setString(1, "%" + criterio + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapper.map(rs));
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarRegistro(String tabla, String campoId, String idTxt, Runnable callback) {
        if (idTxt == null || idTxt.trim().isEmpty()) {
            mostrarAlerta("Validación", "Seleccione un registro para eliminar", Alert.AlertType.WARNING); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar");
        confirm.setContentText("¿Eliminar este registro?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = ConexionBD.conectar()) {
                if (conn == null) return;
                PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tabla + " WHERE " + campoId + "=?");
                ps.setInt(1, Integer.parseInt(idTxt));
                if (ps.executeUpdate() > 0) {
                    mostrarAlerta("Éxito", "Registro eliminado", Alert.AlertType.INFORMATION);
                    callback.run();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void volverMenu(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();
        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
        }
    }

    @FXML
    private void probarConexion() {
        Connection con = ConexionBD.conectar();
        if (con != null) mostrarAlerta("Conexión", "Conexión exitosa", Alert.AlertType.INFORMATION);
        else mostrarAlerta("Error", "No se pudo conectar", Alert.AlertType.ERROR);
    }

    // ================= MODELOS =================
    @FunctionalInterface
    interface ResultSetMapper<T> { T map(ResultSet rs) throws SQLException; }

    public static class OrdenCompra {
        private int id; private String numero; private String fecha; private String nombre; private String tipoProductos; private String estado;
        public OrdenCompra(int id, String numero, String fecha, String nombre, String tipoProductos, String estado) {
            this.id = id; this.numero = numero; this.fecha = fecha; this.nombre = nombre; this.tipoProductos = tipoProductos; this.estado = estado;
        }
        public int getId() { return id; }
        public String getNumero() { return numero; }
        public String getFecha() { return fecha; }
        public String getNombre() { return nombre; }
        public String getTipoProductos() { return tipoProductos; }
        public String getEstado() { return estado; }
    }

    public static class Proveedor {
        private int id; private String codigo; private String nombre; private String telefono; private String tipoProductos; private String estado;
        public Proveedor(int id, String codigo, String nombre, String telefono, String tipoProductos, String estado) {
            this.id = id; this.codigo = codigo; this.nombre = nombre; this.telefono = telefono; this.tipoProductos = tipoProductos; this.estado = estado;
        }
        public int getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getTelefono() { return telefono; }
        public String getTipoProductos() { return tipoProductos; }
        public String getEstado() { return estado; }
        @Override public String toString() { return codigo + " - " + nombre; }
    }

    public static class Recepcion {
        private int id; private String numeroOrden; private String responsable; private String estadoProducto; private double cantidadRecibida;
        public Recepcion(int id, String numeroOrden, String responsable, String estadoProducto, double cantidadRecibida) {
            this.id = id; this.numeroOrden = numeroOrden; this.responsable = responsable; this.estadoProducto = estadoProducto; this.cantidadRecibida = cantidadRecibida;
        }
        public int getId() { return id; }
        public String getNumeroOrden() { return numeroOrden; }
        public String getResponsable() { return responsable; }
        public String getEstadoProducto() { return estadoProducto; }
        public double getCantidadRecibida() { return cantidadRecibida; }
    }

    public static class Ingrediente {
        private int id; private String codigo; private String nombre; private String categoria; private String unidadMedida; private double precioUnitario; private String fechaVencimiento; private String notas;
        public Ingrediente(int id, String codigo, String nombre, String categoria, String unidadMedida, double precioUnitario, String fechaVencimiento, String notas) {
            this.id = id; this.codigo = codigo; this.nombre = nombre; this.categoria = categoria; this.unidadMedida = unidadMedida; this.precioUnitario = precioUnitario; this.fechaVencimiento = fechaVencimiento; this.notas = notas;
        }
        public int getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getUnidadMedida() { return unidadMedida; }
        public double getPrecioUnitario() { return precioUnitario; }
        public String getFechaVencimiento() { return fechaVencimiento; }
        public String getNotas() { return notas; }
    }

    public static class Pago {
        private int id; private String metodoPago; private double monto; private String referencia;
        public Pago(int id, String metodoPago, double monto, String referencia) {
            this.id = id; this.metodoPago = metodoPago; this.monto = monto; this.referencia = referencia;
        }
        public int getId() { return id; }
        public String getMetodoPago() { return metodoPago; }
        public double getMonto() { return monto; }
        public String getReferencia() { return referencia; }
    }
}

