package org.example.salsiaopf.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.salsiaopf.dao.ClienteDAO;
import org.example.salsiaopf.model.Cliente;
import org.example.salsiaopf.util.Alertas;
import org.example.salsiaopf.util.Navegacion;
import org.example.salsiaopf.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClienteController {

    @FXML private ImageView logoImage;
    @FXML private ToggleButton segCliente;
    @FXML private ToggleButton segHistorial;
    @FXML private ToggleButton segRecepcion;
    @FXML private VBox viewCliente;
    @FXML private VBox viewHistorial;
    @FXML private VBox viewRecepcion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private VBox viewRegistro;
    @FXML private VBox viewReservas;
    @FXML private VBox viewReclamaciones;
    @FXML private Label lblFechaActual;
    @FXML private Label lblHoraActual;
    @FXML private Button btnNotificaciones;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, String> colIdCliente;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellido;
    @FXML private TableColumn<Cliente, String> colTelefono;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    private void initialize() {
        cargarLogo();
        iniciarReloj();
        configurarTabla();
        mostrarSolo(viewRegistro);
        cargarClientes();
        actualizarUsuarioSesion();

        if (segCliente != null) {
            segCliente.setSelected(true);
        }
        showOnly(viewCliente);
    }

    private void configurarTabla() {
        if (tablaClientes == null) return;

        if (colIdCliente != null) {
            colIdCliente.setCellValueFactory(cd ->
                    new SimpleStringProperty(String.valueOf(cd.getValue().getIdCliente())));
        }
        if (colNombre != null) {
            colNombre.setCellValueFactory(cd ->
                    new SimpleStringProperty(cd.getValue().getNombre()));
        }
        if (colApellido != null) {
            colApellido.setCellValueFactory(cd ->
                    new SimpleStringProperty(cd.getValue().getApellido()));
        }
        if (colTelefono != null) {
            colTelefono.setCellValueFactory(cd ->
                    new SimpleStringProperty(cd.getValue().getTelefono()));
        }
    }

    private void actualizarUsuarioSesion() {
        var usuario = SessionManager.getInstance().getUsuarioActivo();
        if (usuario != null) {
            System.out.println("[Clientes] Sesión: " + usuario.getNombre() + " (" + usuario.getRol() + ")");
        }
    }

    private void mostrarSolo(VBox vistaActiva) {
        VBox[] vistas = {viewRegistro, viewReservas, viewReclamaciones, viewHistorial};
        for (VBox vista : vistas) {
            boolean activa = (vista == vistaActiva);
            vista.setVisible(activa);
            vista.setManaged(activa);
        }
    }

    private void iniciarReloj() {
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

    @FXML
    private void mostrarRegistro() {
        mostrarSolo(viewRegistro);
    }

    @FXML
    private void mostrarReservas() {
        mostrarSolo(viewReservas);
    }

    @FXML
    private void mostrarReclamaciones() {
        mostrarSolo(viewReclamaciones);
    }

    @FXML
    private void mostrarHistorial() {
        mostrarSolo(viewHistorial);
    }

    @FXML
    private void salirSistema(ActionEvent event) {
        SessionManager.getInstance().cerrarSesion();
        System.exit(0);
    }

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

    private void showOnly(VBox target) {
        VBox[] all = {viewCliente, viewHistorial, viewRecepcion};
        for (VBox v : all) {
            if (v != null) {
                boolean active = (v == target);
                v.setVisible(active);
                v.setManaged(active);
            }
        }
    }

    @FXML
    private void showCliente() {
        showOnly(viewCliente);
    }

    @FXML
    private void showHistorial() {
        showOnly(viewHistorial);
    }

    @FXML
    private void showRecepcion() {
        showOnly(viewRecepcion);
    }

    @FXML
    private void guardarCliente() {
        if (txtNombre == null || txtApellido == null || txtTelefono == null) {
            Alertas.error("Error", "Formulario no vinculado correctamente.");
            return;
        }

        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty()) {
            Alertas.advertencia("Validación", "Complete nombre, apellido y teléfono.");
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setTelefono(telefono);

        if (clienteDAO.guardar(cliente)) {
            if (txtDireccion != null && !txtDireccion.getText().trim().isEmpty()) {
                clienteDAO.guardarDireccion(txtDireccion.getText().trim());
            }
            Alertas.exito("Cliente", "Cliente guardado correctamente en SQL Server.");
            limpiarRegistroCliente();
            cargarClientes();
        } else {
            Alertas.error("Cliente", "No se pudo guardar. Verifique la conexión y la tabla tbl_CLIENTE.");
        }
    }

    @FXML
    private void limpiarRegistroCliente() {
        if (txtNombre != null) txtNombre.clear();
        if (txtApellido != null) txtApellido.clear();
        if (txtTelefono != null) txtTelefono.clear();
        if (txtDireccion != null) txtDireccion.clear();
    }

    @FXML
    private void cargarClientes() {
        if (tablaClientes == null) return;
        tablaClientes.setItems(FXCollections.observableArrayList(clienteDAO.listar()));
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        Navegacion.volverCentroSistema(event);
    }
}
