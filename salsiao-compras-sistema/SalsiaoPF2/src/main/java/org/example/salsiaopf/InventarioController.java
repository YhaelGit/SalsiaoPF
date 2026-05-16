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

public class InventarioController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private VBox viewIngredientes;
    @FXML private VBox viewStock;
    @FXML private VBox viewReportes;

    // === INGREDIENTES ===
    @FXML private TextField txtIngId, txtIngCodigo, txtIngNombre, txtIngCategoria, txtIngUnidad, txtIngProveedor, txtIngEstado;
    @FXML private DatePicker dpIngVencimiento;
    @FXML private TableView<Ingrediente> tablaIngredientes;
    @FXML private TableColumn<Ingrediente, Integer> colIngId;
    @FXML private TableColumn<Ingrediente, String> colIngCodigo, colIngNombre, colIngCategoria, colIngUnidad, colIngProveedor, colIngEstado;

    // === STOCK ===
    @FXML private TextField txtStockIng, txtStockDisp, txtStockMin, txtStockMax;
    @FXML private TableView<StockItem> tablaStock;
    @FXML private TableColumn<StockItem, String> colStockIng, colStockDisp, colStockMin, colStockMax, colStockEstado;

    private ObservableList<Ingrediente> listaIngredientes = FXCollections.observableArrayList();
    private ObservableList<StockItem> listaStock = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        configurarTablas();
        showOnly(viewIngredientes);
        cargarIngredientes();
        cargarStock();
    }

    private void configurarTablas() {
        colIngId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colIngCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colIngNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colIngCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colIngUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colIngProveedor.setCellValueFactory(new PropertyValueFactory<>("proveedor"));
        colIngEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaIngredientes.setItems(listaIngredientes);
        tablaIngredientes.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposIng(nuevo);
        });

        colStockIng.setCellValueFactory(new PropertyValueFactory<>("ingrediente"));
        colStockDisp.setCellValueFactory(new PropertyValueFactory<>("disponible"));
        colStockMin.setCellValueFactory(new PropertyValueFactory<>("minimo"));
        colStockMax.setCellValueFactory(new PropertyValueFactory<>("maximo"));
        colStockEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaStock.setItems(listaStock);
    }

    private void llenarCamposIng(Ingrediente i) {
        txtIngId.setText(String.valueOf(i.getId()));
        txtIngCodigo.setText(i.getCodigo());
        txtIngNombre.setText(i.getNombre());
        txtIngCategoria.setText(i.getCategoria());
        txtIngUnidad.setText(i.getUnidad());
        txtIngProveedor.setText(i.getProveedor());
        txtIngEstado.setText(i.getEstado());
    }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo");
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = {viewIngredientes, viewStock, viewReportes};
        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showIngredientes() { showOnly(viewIngredientes); }
    @FXML private void showStock() { showOnly(viewStock); }
    @FXML private void showReportes() { showOnly(viewReportes); }


    @FXML
    private void guardarIngrediente() {
        if (txtIngNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING);
            return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtIngId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Ingredientes (codigo, nombre, categoria, unidad_medida, estado) VALUES (?,?,?,?,?)"
                : "UPDATE Ingredientes SET codigo=?, nombre=?, categoria=?, unidad_medida=?, estado=? WHERE id_ingrediente=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtIngCodigo.getText());
            ps.setString(2, txtIngNombre.getText());
            ps.setString(3, txtIngCategoria.getText());
            ps.setString(4, txtIngUnidad.getText());
            ps.setString(5, txtIngEstado.getText());
            if (!idTxt.isEmpty()) ps.setInt(6, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Ingrediente guardado", Alert.AlertType.INFORMATION);
            limpiarIng(); cargarIngredientes();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarIngrediente() {
        eliminarRegistro("Ingredientes", "id_ingrediente", txtIngId.getText(), this::cargarIngredientes);
    }

    @FXML
    private void limpiarIng() {
        txtIngId.clear(); txtIngCodigo.clear(); txtIngNombre.clear(); txtIngCategoria.clear();
        txtIngUnidad.clear(); txtIngProveedor.clear(); txtIngEstado.clear();
        dpIngVencimiento.setValue(null);
    }

    private void cargarIngredientes() {
        listaIngredientes.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT i.*, p.nombre as prov_nombre FROM Ingredientes i LEFT JOIN Proveedores p ON i.id_proveedor=p.id_proveedor ORDER BY i.nombre";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                listaIngredientes.add(new Ingrediente(
                    rs.getInt("id_ingrediente"), rs.getString("codigo"), rs.getString("nombre"),
                    rs.getString("categoria"), rs.getString("unidad_medida"),
                    rs.getString("prov_nombre"), rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    @FXML
    private void actualizarStock() {
        mostrarAlerta("Info", "Función de actualizar stock en desarrollo", Alert.AlertType.INFORMATION);
    }

    private void cargarStock() {
        listaStock.clear();
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String sql = "SELECT i.nombre, COALESCE(s.cantidad_disponible,0) as disp, COALESCE(s.cantidad_minima,0) as min, COALESCE(s.cantidad_maxima,0) as max, CASE WHEN COALESCE(s.cantidad_disponible,0) <= COALESCE(s.cantidad_minima,0) THEN 'Bajo' ELSE 'Normal' END as estado FROM Ingredientes i LEFT JOIN Stock s ON i.id_ingrediente=s.id_ingrediente";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                listaStock.add(new StockItem(
                    rs.getString("nombre"), rs.getString("disp"),
                    rs.getString("min"), rs.getString("max"), rs.getString("estado")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void eliminarRegistro(String tabla, String campoId, String idTxt, Runnable callback) {
        if (idTxt == null || idTxt.trim().isEmpty()) {
            mostrarAlerta("Validación", "Seleccione un registro", Alert.AlertType.WARNING); return;
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


    public static class Ingrediente {
        private int id; private String codigo, nombre, categoria, unidad, proveedor, estado;
        public Ingrediente(int id, String codigo, String nombre, String categoria, String unidad, String proveedor, String estado) {
            this.id = id; this.codigo = codigo; this.nombre = nombre; this.categoria = categoria; this.unidad = unidad; this.proveedor = proveedor; this.estado = estado;
        }
        public int getId() { return id; }
        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public String getCategoria() { return categoria; }
        public String getUnidad() { return unidad; }
        public String getProveedor() { return proveedor; }
        public String getEstado() { return estado; }
    }

    public static class StockItem {
        private String ingrediente, disponible, minimo, maximo, estado;
        public StockItem(String ingrediente, String disponible, String minimo, String maximo, String estado) {
            this.ingrediente = ingrediente; this.disponible = disponible; this.minimo = minimo; this.maximo = maximo; this.estado = estado;
        }
        public String getIngrediente() { return ingrediente; }
        public String getDisponible() { return disponible; }
        public String getMinimo() { return minimo; }
        public String getMaximo() { return maximo; }
        public String getEstado() { return estado; }
    }
}