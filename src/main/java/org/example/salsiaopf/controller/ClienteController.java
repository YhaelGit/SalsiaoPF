package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.salsiaopf.dao.ClienteDAO;
import org.example.salsiaopf.model.Cliente;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.ControllerUtil;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ClienteController {

    @FXML private ImageView logoImage;
    @FXML private Label lblFechaActual, lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private StackPane contenedorClientes;
    private ScrollPane[] scrolls;

    // Dashboard
    @FXML private Label lblTotalClientes, lblClientesFrecuentes, lblNuevosMes, lblTotalPedidos, lblTotalReservas, lblTopCompra;
    @FXML private TextField txtBuscarDashboard;
    @FXML private TableView<Cliente> tablaDashboardClientes;
    @FXML private TableColumn<Cliente,String> colDashCodigo, colDashNombre, colDashTelefono, colDashTipo, colDashEstado;
    @FXML private TableColumn<Cliente,String> colDashUltimaCompra;

    // Lista
    @FXML private TableView<Cliente> tablaListaClientes;
    @FXML private TableColumn<Cliente,String> colListaCodigo, colListaNombre, colListaApellido, colListaCedula, colListaTelefono, colListaDireccion, colListaEmail, colListaTipo, colListaEstado;
    @FXML private TextField txtListaBuscar, txtListaCodigo, txtListaNombre, txtListaApellido, txtListaCedula, txtListaTelefono, txtListaDireccion, txtListaEmail, txtListaObservaciones;
    @FXML private ComboBox<String> cmbListaTipo, cmbListaEstado;

    // Registrar
    @FXML private TextField txtRegNombre, txtRegApellido, txtRegCedula, txtRegTelefono, txtRegDireccion, txtRegEmail;
    @FXML private ComboBox<String> cmbRegTipo;
    @FXML private TextArea txtRegObservaciones;

    // Historial
    @FXML private ComboBox<Cliente> cmbHistorialCliente;
    @FXML private Label lblHistTotalCompras, lblHistMontoTotal;
    @FXML private TableView<Map<String,Object>> tablaHistorialCompras;
    @FXML private TableColumn<Map<String,Object>,String> colHistFecha, colHistPedido, colHistTotal, colHistMetodo, colHistEstado;

    // Frecuentes
    @FXML private TableView<Cliente> tablaFrecuentes;
    @FXML private TableColumn<Cliente,String> colFrecCodigo, colFrecNombre, colFrecUltimaCompra, colFrecEstado;
    @FXML private TableColumn<Cliente,Number> colFrecCompras, colFrecTotal;
    @FXML private TextField txtFrecBuscar;

    // Reservas
    @FXML private ComboBox<Cliente> cmbReservaCliente;
    @FXML private DatePicker dpReservaFecha;
    @FXML private TextField txtReservaHora, txtReservaObservaciones, txtReservaBuscar;
    @FXML private Spinner<Integer> spReservaPersonas;
    @FXML private ComboBox<String> cmbReservaEstado;
    @FXML private TableView<Map<String,Object>> tablaReservas;
    @FXML private TableColumn<Map<String,Object>,String> colResCodigo, colResCliente, colResFecha, colResHora, colResPersonas, colResEstado, colResObservaciones;

    // Direcciones
    @FXML private ComboBox<Cliente> cmbDirCliente;
    @FXML private TextField txtDirDireccion, txtDirReferencia, txtDirZona, txtDirTelefono, txtDirBuscar;
    @FXML private TableView<Map<String,Object>> tablaDirecciones;
    @FXML private TableColumn<Map<String,Object>,String> colDirCliente, colDirDireccion, colDirReferencia, colDirZona, colDirTelefono;

    private int clienteEditandoId = -1;
    private int direccionEditandoId = -1;
    private int reservaEditandoId = -1;
    private ObservableList<Map<String,Object>> historialData = FXCollections.observableArrayList();
    private ObservableList<Map<String,Object>> reservasData = FXCollections.observableArrayList();
    private ObservableList<Map<String,Object>> direccionesData = FXCollections.observableArrayList();

    private static final String[] TIPOS = {"Regular","VIP","Corporativo"};
    private static final String[] ESTADOS_CLIENTE = {"Activo","Inactivo","Bloqueado"};
    private static final String[] ESTADOS_RESERVA = {"Pendiente","Confirmada","Cancelada","Completada"};

    @FXML
    private void initialize() {
        ClienteDAO.crearTablasSiNoExisten();
        ClienteDAO.insertarSeedData();
        cargarLogo();
        iniciarReloj();
        scrolls = new ScrollPane[]{scrollDashboard, scrollLista, scrollRegistrar, scrollHistorial, scrollFrecuentes, scrollReservas, scrollDirecciones};
        configurarCombos();
        configurarTablas();
        cmbHistorialCliente.setOnAction(e -> cargarHistorialCliente());
        mostrarSolo(scrollDashboard);
        cargarDashboard();
        cargarListaClientes();
        actualizarUsuarioSesion();
        ControllerUtil.animarEntrada(lblFechaActual);
    }

    // ═══════════ NAVEGACIÓN ═══════════

    @FXML private ScrollPane scrollDashboard, scrollLista, scrollRegistrar, scrollHistorial, scrollFrecuentes, scrollReservas, scrollDirecciones;

    private void mostrarSolo(ScrollPane activo) {
        for (ScrollPane s : scrolls) {
            boolean show = s == activo;
            s.setVisible(show);
            s.setManaged(show);
        }
    }

    @FXML private void mostrarDashboard() { mostrarSolo(scrollDashboard); cargarDashboard(); }
    @FXML private void mostrarLista() { mostrarSolo(scrollLista); cargarListaClientes(); }
    @FXML private void mostrarRegistrar() { mostrarSolo(scrollRegistrar); }
    @FXML private void mostrarHistorial() { mostrarSolo(scrollHistorial); cargarHistorial(); }
    @FXML private void mostrarFrecuentes() { mostrarSolo(scrollFrecuentes); cargarFrecuentes(); }
    @FXML private void mostrarReservas() { mostrarSolo(scrollReservas); cargarReservas(); }
    @FXML private void mostrarDirecciones() { mostrarSolo(scrollDirecciones); cargarDirecciones(); }

    // ═══════════ DASHBOARD ═══════════

    private void cargarDashboard() {
        Map<String,Object> d = ClienteDAO.obtenerConteosDashboard();
        lblTotalClientes.setText(String.valueOf(d.get("totalClientes")));
        lblClientesFrecuentes.setText(String.valueOf(d.get("frecuentes")));
        lblNuevosMes.setText(String.valueOf(d.get("nuevosMes")));
        lblTotalPedidos.setText(String.valueOf(d.get("totalPedidos")));
        lblTotalReservas.setText(String.valueOf(d.get("totalReservas")));
        lblTopCompra.setText("RD$ " + String.format("%.2f", d.get("topCompra")));
        cargarTablaDashboard();
    }

    private void cargarTablaDashboard() {
        List<Cliente> lista = ClienteDAO.listarClientes();
        if (!lista.isEmpty()) lista = lista.size() > 20 ? lista.subList(0, 20) : lista;
        tablaDashboardClientes.setItems(FXCollections.observableArrayList(lista));
    }

    @FXML private void buscarDashboard() {
        String f = txtBuscarDashboard.getText().trim();
        if (f.isEmpty()) { cargarTablaDashboard(); return; }
        tablaDashboardClientes.setItems(FXCollections.observableArrayList(ClienteDAO.buscarClientes(f)));
    }

    @FXML private void limpiarDashboard() {
        txtBuscarDashboard.clear();
        cargarTablaDashboard();
    }

    // ═══════════ LISTA CLIENTES ═══════════

    private void configurarTablas() {
        // Dashboard
        colDashCodigo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCodigo()));
        colDashNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre() + " " + cd.getValue().getApellido()));
        colDashTelefono.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTelefono()));
        colDashTipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTipoCliente()));
        colDashUltimaCompra.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUltimaCompra() != null ? cd.getValue().getUltimaCompra().toString() : ""));
        colDashEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));

        // Lista
        colListaCodigo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCodigo()));
        colListaNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre()));
        colListaApellido.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getApellido()));
        colListaCedula.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCedula()));
        colListaTelefono.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTelefono()));
        colListaDireccion.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDireccion()));
        colListaEmail.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEmail()));
        colListaTipo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTipoCliente()));
        colListaEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));

        // Frecuentes
        colFrecCodigo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCodigo()));
        colFrecNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombre() + " " + cd.getValue().getApellido()));
        colFrecCompras.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getCantidadCompras()));
        colFrecTotal.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getTotalCompras()));
        colFrecUltimaCompra.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUltimaCompra() != null ? cd.getValue().getUltimaCompra().toString() : "N/A"));
        colFrecEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getEstado()));

        // Historial
        colHistFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Fecha") != null ? cd.getValue().get("Fecha").toString() : ""));
        colHistPedido.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Numero_pedido") != null ? cd.getValue().get("Numero_pedido").toString() : ""));
        colHistTotal.setCellValueFactory(cd -> new SimpleStringProperty("RD$ " + String.format("%.2f", cd.getValue().get("Total") != null ? (Double)cd.getValue().get("Total") : 0)));
        colHistMetodo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Metodo_pago") != null ? cd.getValue().get("Metodo_pago").toString() : ""));
        colHistEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Estado_pedido") != null ? cd.getValue().get("Estado_pedido").toString() : ""));

        // Reservas
        colResCodigo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Codigo") != null ? cd.getValue().get("Codigo").toString() : ""));
        colResCliente.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Cliente") != null ? cd.getValue().get("Cliente").toString() : ""));
        colResFecha.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Fecha") != null ? cd.getValue().get("Fecha").toString() : ""));
        colResHora.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Hora") != null ? cd.getValue().get("Hora").toString() : ""));
        colResPersonas.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Personas") != null ? cd.getValue().get("Personas").toString() : ""));
        colResEstado.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Estado") != null ? cd.getValue().get("Estado").toString() : ""));
        colResObservaciones.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Observaciones") != null ? cd.getValue().get("Observaciones").toString() : ""));

        // Direcciones
        colDirCliente.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Cliente") != null ? cd.getValue().get("Cliente").toString() : ""));
        colDirDireccion.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Direccion") != null ? cd.getValue().get("Direccion").toString() : ""));
        colDirReferencia.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Referencia") != null ? cd.getValue().get("Referencia").toString() : ""));
        colDirZona.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Zona") != null ? cd.getValue().get("Zona").toString() : ""));
        colDirTelefono.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().get("Telefono") != null ? cd.getValue().get("Telefono").toString() : ""));
    }

    private void configurarCombos() {
        cmbListaTipo.setItems(FXCollections.observableArrayList(TIPOS));
        cmbListaEstado.setItems(FXCollections.observableArrayList(ESTADOS_CLIENTE));
        cmbRegTipo.setItems(FXCollections.observableArrayList(TIPOS));
        cmbRegTipo.setValue("Regular");
        cmbReservaEstado.setItems(FXCollections.observableArrayList(ESTADOS_RESERVA));
        cmbReservaEstado.setValue("Pendiente");

        SpinnerValueFactory.IntegerSpinnerValueFactory svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 2);
        spReservaPersonas.setValueFactory(svf);
    }

    private void cargarListaClientes() {
        tablaListaClientes.setItems(FXCollections.observableArrayList(ClienteDAO.listarClientes()));
    }

    @FXML private void buscarLista() {
        String f = txtListaBuscar.getText().trim();
        if (f.isEmpty()) { cargarListaClientes(); return; }
        tablaListaClientes.setItems(FXCollections.observableArrayList(ClienteDAO.buscarClientes(f)));
    }

    @FXML private void filtrarLista() {
        String f = txtListaBuscar.getText().trim();
        List<Cliente> todos = ClienteDAO.listarClientes();
        if (!f.isEmpty()) todos = ClienteDAO.buscarClientes(f);
        String tipo = cmbListaTipo.getValue();
        String estado = cmbListaEstado.getValue();
        if (tipo != null && !tipo.isEmpty()) todos = todos.stream().filter(c -> tipo.equals(c.getTipoCliente())).toList();
        if (estado != null && !estado.isEmpty()) todos = todos.stream().filter(c -> estado.equals(c.getEstado())).toList();
        tablaListaClientes.setItems(FXCollections.observableArrayList(todos));
    }

    @FXML private void limpiarLista() {
        txtListaBuscar.clear();
        cmbListaTipo.getSelectionModel().clearSelection();
        cmbListaEstado.getSelectionModel().clearSelection();
        cancelarEdicion();
        cargarListaClientes();
    }

    @FXML private void nuevoCliente() {
        clienteEditandoId = -1;
        txtListaCodigo.setText(ClienteDAO.generarCodigoCliente());
        txtListaNombre.clear(); txtListaApellido.clear(); txtListaCedula.clear();
        txtListaTelefono.clear(); txtListaDireccion.clear(); txtListaEmail.clear();
        txtListaObservaciones.clear();
        cmbListaTipo.setValue("Regular"); cmbListaEstado.setValue("Activo");
    }

    @FXML private void editarCliente() {
        Cliente c = tablaListaClientes.getSelectionModel().getSelectedItem();
        if (c == null) { Alertas.advertencia("Editar", "Seleccione un cliente de la tabla."); return; }
        clienteEditandoId = c.getIdCliente();
        txtListaCodigo.setText(c.getCodigo());
        txtListaNombre.setText(c.getNombre()); txtListaApellido.setText(c.getApellido());
        txtListaCedula.setText(c.getCedula()); txtListaTelefono.setText(c.getTelefono());
        txtListaDireccion.setText(c.getDireccion()); txtListaEmail.setText(c.getEmail());
        txtListaObservaciones.setText(c.getObservaciones());
        cmbListaTipo.setValue(c.getTipoCliente());
        cmbListaEstado.setValue(c.getEstado());
    }

    @FXML private void eliminarCliente() {
        Cliente c = tablaListaClientes.getSelectionModel().getSelectedItem();
        if (c == null) { Alertas.advertencia("Eliminar", "Seleccione un cliente de la tabla."); return; }
        if (!Alertas.confirmar("Eliminar cliente", "¿Está seguro de eliminar a " + c.getNombre() + " " + c.getApellido() + "?\nSe eliminarán también sus direcciones, reservas y compras.")) return;
        if (ClienteDAO.eliminarCliente(c.getIdCliente())) {
            Alertas.exito("Eliminado", "Cliente eliminado correctamente.");
            cargarListaClientes(); cargarDashboard();
        } else Alertas.error("Error", "No se pudo eliminar el cliente.");
    }

    @FXML private void verDetalleCliente() {
        Cliente c = tablaListaClientes.getSelectionModel().getSelectedItem();
        if (c == null) { Alertas.advertencia("Detalle", "Seleccione un cliente."); return; }
        Alertas.informacion("Detalle del cliente",
            "Código: " + c.getCodigo() + "\nNombre: " + c.getNombre() + " " + c.getApellido() +
            "\nCédula: " + c.getCedula() + "\nTeléfono: " + c.getTelefono() +
            "\nDirección: " + c.getDireccion() + "\nEmail: " + c.getEmail() +
            "\nTipo: " + c.getTipoCliente() + "\nEstado: " + c.getEstado() +
            "\nCompras: " + c.getCantidadCompras() + " | Total: RD$ " + String.format("%.2f", c.getTotalCompras()) +
            "\nÚltima compra: " + (c.getUltimaCompra() != null ? c.getUltimaCompra().toString() : "N/A") +
            "\nObservaciones: " + c.getObservaciones());
    }

    @FXML private void guardarListaCliente() {
        String nombre = txtListaNombre.getText().trim();
        String apellido = txtListaApellido.getText().trim();
        String telefono = txtListaTelefono.getText().trim();
        if (nombre.isEmpty() || apellido.isEmpty()) { Alertas.advertencia("Validación", "Nombre y apellido son obligatorios."); return; }

        String codigo = txtListaCodigo.getText().trim();
        if (codigo.isEmpty()) codigo = ClienteDAO.generarCodigoCliente();

        if (clienteEditandoId > 0) {
            if (ClienteDAO.actualizarCliente(clienteEditandoId, nombre, apellido, txtListaCedula.getText().trim(), telefono, txtListaDireccion.getText().trim(), txtListaEmail.getText().trim(), cmbListaTipo.getValue(), cmbListaEstado.getValue(), txtListaObservaciones.getText().trim())) {
                Alertas.exito("Actualizado", "Cliente actualizado correctamente.");
                cancelarEdicion(); cargarListaClientes(); cargarDashboard();
            } else Alertas.error("Error", "No se pudo actualizar.");
        } else {
            int id = ClienteDAO.guardarCliente(codigo, nombre, apellido, txtListaCedula.getText().trim(), telefono, txtListaDireccion.getText().trim(), txtListaEmail.getText().trim(), cmbListaTipo.getValue(), cmbListaEstado.getValue(), txtListaObservaciones.getText().trim());
            if (id > 0) {
                Alertas.exito("Guardado", "Cliente guardado correctamente.");
                nuevoCliente(); cargarListaClientes(); cargarDashboard();
            } else Alertas.error("Error", "No se pudo guardar el cliente.");
        }
    }

    @FXML private void cancelarEdicion() {
        clienteEditandoId = -1;
        txtListaCodigo.clear(); txtListaNombre.clear(); txtListaApellido.clear();
        txtListaCedula.clear(); txtListaTelefono.clear(); txtListaDireccion.clear();
        txtListaEmail.clear(); txtListaObservaciones.clear();
        cmbListaTipo.getSelectionModel().clearSelection();
        cmbListaEstado.getSelectionModel().clearSelection();
    }

    @FXML private void exportarLista() {
        Alertas.informacion("Exportar", "Función de exportación en desarrollo.");
    }

    // ═══════════ REGISTRAR ═══════════

    @FXML private void guardarRegistro() {
        String nombre = txtRegNombre.getText().trim();
        String apellido = txtRegApellido.getText().trim();
        String telefono = txtRegTelefono.getText().trim();
        if (nombre.isEmpty() || apellido.isEmpty()) { Alertas.advertencia("Validación", "Nombre y apellido son obligatorios."); return; }

        String codigo = ClienteDAO.generarCodigoCliente();
        int id = ClienteDAO.guardarCliente(codigo, nombre, apellido, txtRegCedula.getText().trim(), telefono, txtRegDireccion.getText().trim(), txtRegEmail.getText().trim(), cmbRegTipo.getValue(), "Activo", txtRegObservaciones.getText().trim());
        if (id > 0) {
            Alertas.exito("Cliente registrado", "Cliente " + nombre + " " + apellido + " registrado con código " + codigo);
            limpiarRegistro();
            cargarDashboard();
        } else Alertas.error("Error", "No se pudo registrar el cliente.");
    }

    @FXML private void limpiarRegistro() {
        txtRegNombre.clear(); txtRegApellido.clear(); txtRegCedula.clear();
        txtRegTelefono.clear(); txtRegDireccion.clear(); txtRegEmail.clear();
        txtRegObservaciones.clear(); cmbRegTipo.setValue("Regular");
    }

    // ═══════════ HISTORIAL ═══════════

    @FXML private void cargarHistorial() {
        List<Cliente> clientes = ClienteDAO.listarClientes();
        cmbHistorialCliente.setItems(FXCollections.observableArrayList(clientes));
        if (!clientes.isEmpty()) cmbHistorialCliente.getSelectionModel().selectFirst();
        cargarHistorialCliente();
    }

    private void cargarHistorialCliente() {
        Cliente sel = cmbHistorialCliente.getValue();
        System.out.println("[Historial] getValue() = " + (sel != null ? sel.getNombre() + " (ID=" + sel.getIdCliente() + ")" : "NULL"));
        if (sel == null) return;
        var compras = ClienteDAO.listarHistorialCompras(sel.getIdCliente());
        System.out.println("[Historial] listarHistorialCompras returned " + compras.size() + " rows");
        historialData.setAll(compras);
        tablaHistorialCompras.setItems(historialData);
        int totalComp = historialData.size();
        double monto = 0;
        try { monto = historialData.stream().mapToDouble(m -> ((Number)m.getOrDefault("Total", 0.0)).doubleValue()).sum(); } catch (Exception e) { System.out.println("[Historial] Error sum: " + e.getMessage()); }
        lblHistTotalCompras.setText("Total compras: " + totalComp);
        lblHistMontoTotal.setText("Monto total: RD$ " + String.format("%.2f", monto));
    }

    @FXML private void filtrarHistorial() { cargarHistorial(); }

    @FXML private void limpiarHistorial() {
        cmbHistorialCliente.getSelectionModel().clearSelection();
        historialData.clear();
        lblHistTotalCompras.setText("Total compras: 0");
        lblHistMontoTotal.setText("Monto total: RD$ 0.00");
    }

    @FXML private void verDetalleHistorial() {
        Map<String,Object> m = tablaHistorialCompras.getSelectionModel().getSelectedItem();
        if (m == null) { Alertas.advertencia("Detalle", "Seleccione una compra."); return; }
        Alertas.informacion("Detalle de compra",
            "N° Pedido: " + m.get("Numero_pedido") + "\nFecha: " + m.get("Fecha") +
            "\nTotal: RD$ " + String.format("%.2f", (Double)m.getOrDefault("Total",0.0)) +
            "\nMétodo pago: " + m.get("Metodo_pago") + "\nEstado: " + m.get("Estado_pedido"));
    }

    @FXML private void exportarHistorial() {
        Alertas.informacion("Exportar", "Función de exportación en desarrollo.");
    }

    // ═══════════ FRECUENTES ═══════════

    private void cargarFrecuentes() {
        tablaFrecuentes.setItems(FXCollections.observableArrayList(ClienteDAO.listarClientesFrecuentes()));
    }

    @FXML private void buscarFrecuentes() {
        String f = txtFrecBuscar.getText().trim();
        if (f.isEmpty()) { cargarFrecuentes(); return; }
        List<Cliente> todos = ClienteDAO.listarClientesFrecuentes();
        todos = todos.stream().filter(c -> (c.getNombre() + " " + c.getApellido()).toLowerCase().contains(f.toLowerCase())).toList();
        tablaFrecuentes.setItems(FXCollections.observableArrayList(todos));
    }

    @FXML private void filtrarFrecuentes() { buscarFrecuentes(); }

    @FXML private void limpiarFrecuentes() {
        txtFrecBuscar.clear();
        cargarFrecuentes();
    }

    @FXML private void verDetalleFrecuente() {
        Cliente c = tablaFrecuentes.getSelectionModel().getSelectedItem();
        if (c == null) { Alertas.advertencia("Detalle", "Seleccione un cliente."); return; }
        Alertas.informacion("Cliente frecuente",
            c.getNombre() + " " + c.getApellido() + "\nCódigo: " + c.getCodigo() +
            "\nCompras realizadas: " + c.getCantidadCompras() +
            "\nTotal gastado: RD$ " + String.format("%.2f", c.getTotalCompras()) +
            "\nÚltima compra: " + (c.getUltimaCompra() != null ? c.getUltimaCompra() : "N/A") +
            "\nEstado: " + c.getEstado());
    }

    @FXML private void exportarFrecuentes() {
        Alertas.informacion("Exportar", "Función de exportación en desarrollo.");
    }

    // ═══════════ RESERVAS ═══════════

    private void cargarReservas() {
        List<Cliente> clientes = ClienteDAO.listarClientes();
        cmbReservaCliente.setItems(FXCollections.observableArrayList(clientes));
        reservasData.setAll(ClienteDAO.listarReservas());
        tablaReservas.setItems(reservasData);
    }

    @FXML private void nuevaReserva() {
        reservaEditandoId = -1;
        cmbReservaCliente.getSelectionModel().clearSelection();
        dpReservaFecha.setValue(LocalDate.now());
        txtReservaHora.clear();
        spReservaPersonas.getValueFactory().setValue(2);
        cmbReservaEstado.setValue("Pendiente");
        txtReservaObservaciones.clear();
    }

    @FXML private void guardarReserva() {
        Cliente cl = cmbReservaCliente.getValue();
        if (cl == null) { Alertas.advertencia("Reserva", "Seleccione un cliente."); return; }
        LocalDate fecha = dpReservaFecha.getValue();
        String hora = txtReservaHora.getText().trim();
        if (fecha == null || hora.isEmpty()) { Alertas.advertencia("Reserva", "Fecha y hora son obligatorias."); return; }
        int personas = spReservaPersonas.getValue();
        String estado = cmbReservaEstado.getValue() != null ? cmbReservaEstado.getValue() : "Pendiente";

        if (reservaEditandoId > 0) {
            if (ClienteDAO.actualizarReserva(reservaEditandoId, fecha, hora, personas, estado, txtReservaObservaciones.getText().trim())) {
                Alertas.exito("Reserva actualizada", "Reserva actualizada correctamente.");
                cargarReservas(); cargarDashboard();
            } else Alertas.error("Error", "No se pudo actualizar.");
        } else {
            int id = ClienteDAO.guardarReserva(cl.getIdCliente(), fecha, hora, personas, estado, txtReservaObservaciones.getText().trim());
            if (id > 0) {
                Alertas.exito("Reserva creada", "Reserva creada correctamente.");
                nuevaReserva(); cargarReservas(); cargarDashboard();
            } else Alertas.error("Error", "No se pudo crear la reserva.");
        }
    }

    @FXML private void cancelarReserva() {
        Map<String,Object> r = tablaReservas.getSelectionModel().getSelectedItem();
        if (r == null) { Alertas.advertencia("Cancelar", "Seleccione una reserva."); return; }
        if (!Alertas.confirmar("Cancelar reserva", "¿Está seguro de cancelar esta reserva?")) return;
        int id = (Integer)r.get("ID_reserva");
        if (ClienteDAO.eliminarReserva(id)) {
            Alertas.exito("Cancelada", "Reserva cancelada correctamente.");
            cargarReservas(); cargarDashboard();
        } else Alertas.error("Error", "No se pudo cancelar la reserva.");
    }

    @FXML private void verDetalleReserva() {
        Map<String,Object> r = tablaReservas.getSelectionModel().getSelectedItem();
        if (r == null) { Alertas.advertencia("Detalle", "Seleccione una reserva."); return; }
        Alertas.informacion("Detalle de reserva",
            "Código: " + r.get("Codigo") + "\nCliente: " + r.get("Cliente") +
            "\nFecha: " + r.get("Fecha") + "\nHora: " + r.get("Hora") +
            "\nPersonas: " + r.get("Personas") + "\nEstado: " + r.get("Estado") +
            "\nObservaciones: " + r.getOrDefault("Observaciones",""));
    }

    @FXML private void buscarReservas() {
        String f = txtReservaBuscar.getText().trim();
        if (f.isEmpty()) { cargarReservas(); return; }
        List<Map<String,Object>> filtradas = ClienteDAO.listarReservas().stream()
            .filter(m -> m.getOrDefault("Cliente","").toString().toLowerCase().contains(f.toLowerCase()) ||
                         m.getOrDefault("Codigo","").toString().toLowerCase().contains(f.toLowerCase()))
            .toList();
        reservasData.setAll(filtradas);
    }

    @FXML private void limpiarReservas() {
        txtReservaBuscar.clear();
        nuevaReserva();
        cargarReservas();
    }

    @FXML private void cancelarEdicionReserva() { nuevaReserva(); }

    @FXML private void exportarReservas() {
        Alertas.informacion("Exportar", "Función de exportación en desarrollo.");
    }

    // ═══════════ DIRECCIONES ═══════════

    private void cargarDirecciones() {
        List<Cliente> clientes = ClienteDAO.listarClientes();
        cmbDirCliente.setItems(FXCollections.observableArrayList(clientes));
        direccionesData.setAll(ClienteDAO.listarDirecciones());
        tablaDirecciones.setItems(direccionesData);
    }

    @FXML private void nuevaDireccion() {
        direccionEditandoId = -1;
        cmbDirCliente.getSelectionModel().clearSelection();
        txtDirDireccion.clear(); txtDirReferencia.clear();
        txtDirZona.clear(); txtDirTelefono.clear();
    }

    @FXML private void guardarDireccion() {
        Cliente cl = cmbDirCliente.getValue();
        if (cl == null) { Alertas.advertencia("Dirección", "Seleccione un cliente."); return; }
        String dir = txtDirDireccion.getText().trim();
        if (dir.isEmpty()) { Alertas.advertencia("Dirección", "La dirección es obligatoria."); return; }

        if (direccionEditandoId > 0) {
            if (ClienteDAO.actualizarDireccion(direccionEditandoId, dir, txtDirReferencia.getText().trim(), txtDirZona.getText().trim(), txtDirTelefono.getText().trim())) {
                Alertas.exito("Actualizada", "Dirección actualizada correctamente.");
                cargarDirecciones();
            } else Alertas.error("Error", "No se pudo actualizar.");
        } else {
            int id = ClienteDAO.guardarDireccion(cl.getIdCliente(), dir, txtDirReferencia.getText().trim(), txtDirZona.getText().trim(), txtDirTelefono.getText().trim());
            if (id > 0) {
                Alertas.exito("Guardada", "Dirección guardada correctamente.");
                nuevaDireccion(); cargarDirecciones();
            } else Alertas.error("Error", "No se pudo guardar la dirección.");
        }
    }

    @FXML private void editarDireccion() {
        Map<String,Object> m = tablaDirecciones.getSelectionModel().getSelectedItem();
        if (m == null) { Alertas.advertencia("Editar", "Seleccione una dirección."); return; }
        direccionEditandoId = (Integer)m.get("ID_direccion");
        txtDirDireccion.setText(m.get("Direccion").toString());
        txtDirReferencia.setText(m.get("Referencia").toString());
        txtDirZona.setText(m.get("Zona").toString());
        txtDirTelefono.setText(m.get("Telefono").toString());
        // Find the client by name and select it
        String nombreCliente = m.get("Cliente").toString();
        cmbDirCliente.getItems().stream()
            .filter(c -> (c.getNombre() + " " + c.getApellido()).equals(nombreCliente))
            .findFirst().ifPresent(c -> cmbDirCliente.setValue(c));
    }

    @FXML private void eliminarDireccion() {
        Map<String,Object> m = tablaDirecciones.getSelectionModel().getSelectedItem();
        if (m == null) { Alertas.advertencia("Eliminar", "Seleccione una dirección."); return; }
        if (!Alertas.confirmar("Eliminar dirección", "¿Está seguro de eliminar esta dirección?")) return;
        if (ClienteDAO.eliminarDireccion((Integer)m.get("ID_direccion"))) {
            Alertas.exito("Eliminada", "Dirección eliminada correctamente.");
            cargarDirecciones();
        } else Alertas.error("Error", "No se pudo eliminar.");
    }

    @FXML private void cancelarEdicionDireccion() { nuevaDireccion(); }

    @FXML private void buscarDirecciones() {
        String f = txtDirBuscar.getText().trim();
        if (f.isEmpty()) { cargarDirecciones(); return; }
        List<Map<String,Object>> filtradas = ClienteDAO.listarDirecciones().stream()
            .filter(m -> m.getOrDefault("Cliente","").toString().toLowerCase().contains(f.toLowerCase()) ||
                         m.getOrDefault("Direccion","").toString().toLowerCase().contains(f.toLowerCase()) ||
                         m.getOrDefault("Zona","").toString().toLowerCase().contains(f.toLowerCase()))
            .toList();
        direccionesData.setAll(filtradas);
    }

    @FXML private void filtrarDirecciones() { buscarDirecciones(); }

    @FXML private void limpiarDirecciones() {
        txtDirBuscar.clear();
        nuevaDireccion();
        cargarDirecciones();
    }

    // ═══════════ UTILIDADES ═══════════

    private void iniciarReloj() { ControllerUtil.iniciarReloj(lblFechaActual, lblHoraActual); }
    private void cargarLogo() { ControllerUtil.cargarLogo(logoImage); }
    private void actualizarUsuarioSesion() {
        var usuario = SessionManager.getInstance().getUsuarioActivo();
        if (usuario != null) System.out.println("[Clientes] Sesión: " + usuario.getNombre() + " (" + usuario.getRol() + ")");
    }

    @FXML private void mostrarNotificaciones() { Alertas.advertencia("Notificaciones", "Módulo de notificaciones en desarrollo."); }
    @FXML private void volverMenu(ActionEvent event) { Navegacion.volverCentroSistema(event); }
    @FXML private void salirSistema() {
        SessionManager.getInstance().cerrarSesion();
        System.exit(0);
    }
}
