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
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class VentaController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private VBox viewVenta;
    @FXML private VBox viewDetalle;
    @FXML private VBox viewPago;
    @FXML private VBox viewHistorial;

    // === VENTA ===
    @FXML private TextField txtVentaId, txtVentaNumero, txtVentaHora, txtVentaSubtotal, txtVentaItbis, txtVentaTotal, txtVentaObs;
    @FXML private DatePicker dpVentaFecha;
    @FXML private ComboBox<Cliente> cbVentaCliente;
    @FXML private TableView<Venta> tablaVentas;

    // === PAGO ===
    @FXML private TextField txtPagoId, txtPagoMetodo, txtPagoMonto, txtPagoCambio;
    @FXML private DatePicker dpPagoFecha;
    @FXML private ComboBox<String> cbPagoMetodo;

    private ObservableList<Venta> listaVentas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        cargarCombos();
        showOnly(viewVenta);
        cargarVentas();
    }

    private void cargarCombos() {
        cbPagoMetodo.setItems(FXCollections.observableArrayList("Efectivo", "Tarjeta", "Transferencia"));
        cargarClientesCombo();
    }

    private void cargarClientesCombo() {
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT id_cliente, nombre FROM Clientes WHERE estado='Activo'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            ObservableList<Cliente> clientes = FXCollections.observableArrayList();
            while (rs.next()) {
                clientes.add(new Cliente(rs.getInt("id_cliente"), rs.getString("nombre")));
            }
            cbVentaCliente.setItems(clientes);
        } catch (SQLException e) {
            System.out.println("Error cargando clientes: " + e.getMessage());
        }
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                Circle clip = new Circle(); clip.setRadius(35); clip.setCenterX(35); clip.setCenterY(35);
                logoImage.setClip(clip);
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] views = {viewVenta, viewDetalle, viewPago, viewHistorial};
        for (VBox v : views) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showVenta() { showOnly(viewVenta); cargarClientesCombo(); }
    @FXML private void showDetalle() { showOnly(viewDetalle); }
    @FXML private void showPago() { showOnly(viewPago); }
    @FXML private void showHistorial() { showOnly(viewHistorial); }

    // ================= VENTA CRUD =================
    @FXML
    private void guardarVenta() {
        if (txtVentaNumero.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "Número de venta obligatorio", Alert.AlertType.WARNING);
            return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtVentaId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Ventas (numero_venta, fecha, hora, id_cliente, subtotal, itbis, total, observaciones, estado) VALUES (?,?,?,?,?,?,?,?,?)"
                : "UPDATE Ventas SET numero_venta=?, fecha=?, hora=?, id_cliente=?, subtotal=?, itbis=?, total=?, observaciones=? WHERE id_venta=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtVentaNumero.getText());
            ps.setDate(2, dpVentaFecha.getValue() != null ? Date.valueOf(dpVentaFecha.getValue()) : null);
            ps.setString(3, txtVentaHora.getText());
            Cliente c = cbVentaCliente.getValue();
            ps.setObject(4, c != null ? c.getId() : null);
            ps.setString(5, txtVentaSubtotal.getText());
            ps.setString(6, txtVentaItbis.getText());
            ps.setString(7, txtVentaTotal.getText());
            ps.setString(8, txtVentaObs.getText());
            if (idTxt.isEmpty()) ps.setString(9, "Completada");
            else ps.setInt(9, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Venta guardada", Alert.AlertType.INFORMATION);
            limpiarVenta(); cargarVentas();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarVenta() {
        eliminarRegistro("Ventas", "id_venta", txtVentaId.getText(), this::cargarVentas);
    }

    @FXML
    private void limpiarVenta() {
        txtVentaId.clear(); txtVentaNumero.clear(); txtVentaHora.clear();
        txtVentaSubtotal.clear(); txtVentaItbis.clear(); txtVentaTotal.clear(); txtVentaObs.clear();
        dpVentaFecha.setValue(null); cbVentaCliente.setValue(null);
    }

    private void cargarVentas() {
        listaVentas.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT v.*, c.nombre as cliente_nombre FROM Ventas v LEFT JOIN Clientes c ON v.id_cliente=c.id_cliente ORDER BY v.fecha DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                listaVentas.add(new Venta(
                    rs.getInt("id_venta"), rs.getString("numero_venta"),
                    rs.getDate("fecha") != null ? rs.getDate("fecha").toString() : "",
                    rs.getString("cliente_nombre"), rs.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ================= PAGO =================
    @FXML
    private void registrarPago() {
        mostrarAlerta("Info", "Pago registrado", Alert.AlertType.INFORMATION);
        limpiarPago();
    }

    @FXML
    private void limpiarPago() {
        txtPagoId.clear(); txtPagoMetodo.clear(); txtPagoMonto.clear(); txtPagoCambio.clear();
        dpPagoFecha.setValue(null); cbPagoMetodo.setValue(null);
    }

    // ================= UTILIDADES =================
    private void eliminarRegistro(String tabla, String campoId, String idTxt, Runnable callback) {
        if (idTxt == null || idTxt.trim().isEmpty()) {
            mostrarAlerta("Validación", "Seleccione registro", Alert.AlertType.WARNING); return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar"); confirm.setContentText("¿Eliminar?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try (Connection conn = ConexionBD.conectar()) { if (conn == null) return;
                PreparedStatement ps = conn.prepareStatement("DELETE FROM " + tabla + " WHERE " + campoId + "=?");
                ps.setInt(1, Integer.parseInt(idTxt));
                if (ps.executeUpdate() > 0) { mostrarAlerta("Éxito", "Eliminado", Alert.AlertType.INFORMATION); callback.run(); }
            } catch (SQLException e) { mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR); }
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo); alert.setTitle(titulo); alert.setHeaderText(null); alert.setContentText(mensaje); alert.showAndWait();
    }

    @FXML
    private void volverMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene); stage.setTitle("Salsiao - Sistema Principal"); stage.show();
        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
        }
    }

    // ================= MODELOS =================
    public static class Cliente { private int id; private String nombre; public Cliente(int id, String nombre) { this.id=id; this.nombre=nombre; } public int getId() { return id; } public String getNombre() { return nombre; } @Override public String toString() { return nombre; } }
    public static class Venta { private int id; private String numero, fecha, cliente; private double total; public Venta(int id, String numero, String fecha, String cliente, double total) { this.id=id; this.numero=numero; this.fecha=fecha; this.cliente=cliente; this.total=total; } public int getId() { return id; } public String getNumero() { return numero; } public String getFecha() { return fecha; } public String getCliente() { return cliente; } public double getTotal() { return total; } }
}