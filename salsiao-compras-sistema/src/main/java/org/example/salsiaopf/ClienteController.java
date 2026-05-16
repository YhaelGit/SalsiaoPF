package org.example.salsiaopf;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class ClienteController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segCliente;
    @FXML private ToggleButton segHistorial;
    @FXML private ToggleButton segRecepcion;

    @FXML private VBox viewCliente;
    @FXML private VBox viewHistorial;
    @FXML private VBox viewRecepcion;

    @FXML private TextField txtIdCliente;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtBuscarCliente;

    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colEstado;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private Cliente clienteSeleccionado = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        configurarTabla();
        if (segCliente != null) segCliente.setSelected(true);
        showOnly(viewCliente);
        cargarClientes();

        tablaClientes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                clienteSeleccionado = newSel;
                llenarCamposCliente(newSel);
            }
        });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaClientes.setItems(listaClientes);
    }

    private void llenarCamposCliente(Cliente cliente) {
        txtIdCliente.setText(String.valueOf(cliente.getIdCliente()));
        txtNombre.setText(cliente.getNombre());
        txtTelefono.setText(cliente.getTelefono());
        txtEmail.setText(cliente.getEmail());
        txtDireccion.setText(cliente.getDireccion());
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = { viewCliente, viewHistorial, viewRecepcion };
        for (VBox v : all) {
            if (v != null) {
                boolean active = (v == target);
                v.setVisible(active);
                v.setManaged(active);
            }
        }
    }

    @FXML private void showCliente() { showOnly(viewCliente); }
    @FXML private void showHistorial() { showOnly(viewHistorial); }
    @FXML private void showRecepcion() { showOnly(viewRecepcion); }

    @FXML
    private void guardarCliente() {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String email = txtEmail.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) {
                mostrarAlerta("Error", "No hay conexión a la BD", Alert.AlertType.ERROR);
                return;
            }

            String idTexto = txtIdCliente.getText().trim();
            if (idTexto.isEmpty()) {
                String sql = "INSERT INTO Clientes (nombre, telefono, email, direccion, estado) VALUES (?, ?, ?, ?, 'Activo')";
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, email);
                ps.setString(4, direccion);
                ps.executeUpdate();
                mostrarAlerta("Éxito", "Cliente guardado correctamente", Alert.AlertType.INFORMATION);
            } else {
                int id = Integer.parseInt(idTexto);
                String sql = "UPDATE Clientes SET nombre=?, telefono=?, email=?, direccion=? WHERE id_cliente=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, telefono);
                ps.setString(3, email);
                ps.setString(4, direccion);
                ps.setInt(5, id);
                ps.executeUpdate();
                mostrarAlerta("Éxito", "Cliente actualizado correctamente", Alert.AlertType.INFORMATION);
            }
            limpiarCamposCliente();
            cargarClientes();
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarCliente() {
        String idTexto = txtIdCliente.getText().trim();
        if (idTexto.isEmpty()) {
            mostrarAlerta("Validación", "Seleccione un cliente para eliminar", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar cliente?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            try (Connection conn = ConexionBD.conectar()) {
                if (conn == null) return;
                int id = Integer.parseInt(idTexto);
                String sql = "DELETE FROM Clientes WHERE id_cliente=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, id);
                int filas = ps.executeUpdate();
                if (filas > 0) {
                    mostrarAlerta("Éxito", "Cliente eliminado correctamente", Alert.AlertType.INFORMATION);
                    limpiarCamposCliente();
                    cargarClientes();
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Error al eliminar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void buscarCliente() {
        String criterio = txtBuscarCliente.getText().trim();
        if (criterio.isEmpty()) {
            cargarClientes();
            return;
        }
        listaClientes.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT * FROM Clientes WHERE nombre LIKE ? OR telefono LIKE ? OR email LIKE ? ORDER BY nombre";
            PreparedStatement ps = conn.prepareStatement(sql);
            String like = "%" + criterio + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listaClientes.add(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion"),
                    rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al buscar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void limpiarCamposCliente() {
        txtIdCliente.clear();
        txtNombre.clear();
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        clienteSeleccionado = null;
        tablaClientes.getSelectionModel().clearSelection();
    }

    private void cargarClientes() {
        listaClientes.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT * FROM Clientes ORDER BY nombre";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                listaClientes.add(new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("nombre"),
                    rs.getString("telefono"),
                    rs.getString("email"),
                    rs.getString("direccion"),
                    rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Error al cargar clientes: " + e.getMessage(), Alert.AlertType.ERROR);
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
    private void volverMenu(ActionEvent event) {
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

    public static class Cliente {
        private int idCliente;
        private String nombre;
        private String telefono;
        private String email;
        private String direccion;
        private String estado;

        public Cliente(int idCliente, String nombre, String telefono, String email, String direccion, String estado) {
            this.idCliente = idCliente;
            this.nombre = nombre;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
            this.estado = estado;
        }

        public int getIdCliente() { return idCliente; }
        public void setIdCliente(int idCliente) { this.idCliente = idCliente; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}