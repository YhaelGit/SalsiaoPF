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

public class EmpleadosController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segEmpleado;
    @FXML private ToggleButton segDocumentos;
    @FXML private ToggleButton segContratacion;
    @FXML private ToggleButton segCapacitacion;
    @FXML private ToggleButton segConflictos;

    @FXML private VBox viewEmpleado;
    @FXML private VBox viewDocumentos;
    @FXML private VBox viewContratacion;
    @FXML private VBox viewCapacitacion;
    @FXML private VBox viewConflictos;

    // === EMPLEADO ===
    @FXML private TextField txtEmpId, txtEmpCedula, txtEmpNombre, txtEmpApellido, txtEmpTelefono, txtEmpEmail, txtEmpDireccion, txtEmpCargo, txtEmpDepartamento, txtEmpSalario;
    @FXML private DatePicker dpEmpFechaIngreso;
    @FXML private ComboBox<String> cbEmpTipoContrato, cbEmpEstado;
    @FXML private TableView<Empleado> tablaEmpleados;
    @FXML private TableColumn<Empleado, Integer> colEmpId;
    @FXML private TableColumn<Empleado, String> colEmpCedula, colEmpNombre, colEmpApellido, colEmpCargo, colEmpDepartamento, colEmpEstado;
    @FXML private TextField txtBuscarEmp;

    // === DOCUMENTOS ===
    @FXML private TextField txtDocId, txtDocObservaciones;
    @FXML private ComboBox<Empleado> cbDocEmpleado;
    @FXML private ComboBox<String> cbDocTipo, cbDocEstado;
    @FXML private DatePicker dpDocFecha;
    @FXML private TableView<Documento> tablaDocumentos;
    @FXML private TableColumn<Documento, Integer> colDocId;
    @FXML private TableColumn<Documento, String> colDocEmpleado, colDocTipo, colDocEstado;

    // === CONTRATACION ===
    @FXML private TextField txtContId, txtContObservaciones;
    @FXML private ComboBox<Empleado> cbContEmpleado;
    @FXML private ComboBox<String> cbContEstado;
    @FXML private DatePicker dpContFecha;
    @FXML private TableView<Contratacion> tablaContrataciones;

    // === CAPACITACION ===
    @FXML private TextField txtCapId, txtCapPeriodo, txtCapCalificacion;
    @FXML private ComboBox<Empleado> cbCapEmpleado, cbCapMentor;
    @FXML private ComboBox<String> cbCapEstado;
    @FXML private TableView<Capacitacion> tablaCapacitaciones;

    // === CONFLICTOS ===
    @FXML private TextField txtConfId, txtConfObservaciones;
    @FXML private ComboBox<Empleado> cbConfEmpleado;
    @FXML private ComboBox<String> cbConfTipo, cbConfEstado;
    @FXML private DatePicker dpConfFecha;
    @FXML private TableView<Conflicto> tablaConflictos;

    private ObservableList<Empleado> listaEmpleados = FXCollections.observableArrayList();
    private ObservableList<Documento> listaDocumentos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        configurarTablas();
        cargarCombos();
        if (segEmpleado != null) segEmpleado.setSelected(true);
        showOnly(viewEmpleado);
        cargarEmpleados();
    }

    private void cargarCombos() {
        cbEmpTipoContrato.setItems(FXCollections.observableArrayList("Fijo", "Temporal", "Por horas"));
        cbEmpEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo", "Suspendido"));
        cbDocTipo.setItems(FXCollections.observableArrayList("Contrato", "Carta", "Baja", "Recomendación"));
        cbDocEstado.setItems(FXCollections.observableArrayList("Activo", "Archivado", "Emitido"));
        cbContEstado.setItems(FXCollections.observableArrayList("Pendiente", "En proceso", "Contratado"));
        cbCapEstado.setItems(FXCollections.observableArrayList("Pendiente", "En seguimiento", "Finalizado"));
        cbConfTipo.setItems(FXCollections.observableArrayList("Conflicto", "Renuncia", "Baja"));
        cbConfEstado.setItems(FXCollections.observableArrayList("Pendiente", "Resuelto", "Cerrado"));
    }

    private void configurarTablas() {
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmpCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colEmpNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmpApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colEmpCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colEmpDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colEmpEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaEmpleados.setItems(listaEmpleados);
        tablaEmpleados.getSelectionModel().selectedItemProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) llenarCamposEmpleado(nuevo);
        });
    }

    private void llenarCamposEmpleado(Empleado e) {
        txtEmpId.setText(String.valueOf(e.getId()));
        txtEmpCedula.setText(e.getCedula());
        txtEmpNombre.setText(e.getNombre());
        txtEmpApellido.setText(e.getApellido());
        txtEmpTelefono.setText(e.getTelefono());
        txtEmpEmail.setText(e.getEmail());
        txtEmpDireccion.setText(e.getDireccion());
        txtEmpCargo.setText(e.getCargo());
        txtEmpDepartamento.setText(e.getDepartamento());
        txtEmpSalario.setText(e.getSalario());
        cbEmpEstado.setValue(e.getEstado());
    }

    private void cargarLogo() {
        try {
            if (logoImage != null) {
                var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
                if (stream != null) {
                    logoImage.setImage(new Image(stream));
                    logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
                }
            }
        } catch (Exception e) {
            System.out.println("Error cargando logo: " + e.getMessage());
        }
    }

    private void showOnly(VBox target) {
        VBox[] all = { viewEmpleado, viewDocumentos, viewContratacion, viewCapacitacion, viewConflictos };
        for (VBox v : all) {
            boolean active = (v == target);
            v.setVisible(active);
            v.setManaged(active);
        }
    }

    @FXML private void showEmpleado() { showOnly(viewEmpleado); }
    @FXML private void showDocumentos() { showOnly(viewDocumentos); }
    @FXML private void showContratacion() { showOnly(viewContratacion); }
    @FXML private void showCapacitacion() { showOnly(viewCapacitacion); }
    @FXML private void showConflictos() { showOnly(viewConflictos); }

    // ================= EMPLEADO CRUD =================
    @FXML
    private void guardarEmpleado() {
        if (txtEmpNombre.getText().trim().isEmpty()) {
            mostrarAlerta("Validación", "El nombre es obligatorio", Alert.AlertType.WARNING);
            return;
        }
        try (Connection conn = ConexionBD.conectar()) {
            if (conn == null) return;
            String idTxt = txtEmpId.getText().trim();
            String sql = idTxt.isEmpty()
                ? "INSERT INTO Empleados (cedula, nombre, apellido, telefono, email, direccion, fecha_ingreso, cargo, departamento, salario, tipo_contrato, estado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"
                : "UPDATE Empleados SET cedula=?, nombre=?, apellido=?, telefono=?, email=?, direccion=?, fecha_ingreso=?, cargo=?, departamento=?, salario=?, tipo_contrato=?, estado=? WHERE id_empleado=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, txtEmpCedula.getText());
            ps.setString(2, txtEmpNombre.getText());
            ps.setString(3, txtEmpApellido.getText());
            ps.setString(4, txtEmpTelefono.getText());
            ps.setString(5, txtEmpEmail.getText());
            ps.setString(6, txtEmpDireccion.getText());
            ps.setDate(7, dpEmpFechaIngreso.getValue() != null ? Date.valueOf(dpEmpFechaIngreso.getValue()) : null);
            ps.setString(8, txtEmpCargo.getText());
            ps.setString(9, txtEmpDepartamento.getText());
            ps.setString(10, txtEmpSalario.getText());
            ps.setString(11, cbEmpTipoContrato.getValue());
            ps.setString(12, cbEmpEstado.getValue());
            if (!idTxt.isEmpty()) ps.setInt(13, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Empleado guardado", Alert.AlertType.INFORMATION);
            limpiarEmpleado(); cargarEmpleados();
        } catch (SQLException e) {
            mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarEmpleado() {
        eliminarRegistro("Empleados", "id_empleado", txtEmpId.getText(), this::cargarEmpleados);
    }

    @FXML
    private void limpiarEmpleado() {
        txtEmpId.clear(); txtEmpCedula.clear(); txtEmpNombre.clear(); txtEmpApellido.clear();
        txtEmpTelefono.clear(); txtEmpEmail.clear(); txtEmpDireccion.clear();
        txtEmpCargo.clear(); txtEmpDepartamento.clear(); txtEmpSalario.clear();
        dpEmpFechaIngreso.setValue(null); cbEmpTipoContrato.setValue(null); cbEmpEstado.setValue(null);
    }

    @FXML
    private void buscarEmpleado() {
        buscarEnTabla("Empleados", "nombre", txtBuscarEmp.getText(),
            rs -> new Empleado(rs.getInt("id_empleado"), rs.getString("cedula"), rs.getString("nombre"),
                rs.getString("apellido"), rs.getString("telefono"), rs.getString("email"),
                rs.getString("direccion"), rs.getString("cargo"), rs.getString("departamento"),
                rs.getString("salario"), rs.getString("estado")),
            listaEmpleados);
    }

    private void cargarEmpleados() {
        cargarTabla("SELECT * FROM Empleados ORDER BY nombre",
            rs -> new Empleado(rs.getInt("id_empleado"), rs.getString("cedula"), rs.getString("nombre"),
                rs.getString("apellido"), rs.getString("telefono"), rs.getString("email"),
                rs.getString("direccion"), rs.getString("cargo"), rs.getString("departamento"),
                rs.getString("salario"), rs.getString("estado")),
            listaEmpleados);
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
            URL fxml = getClass().getResource("main.fxml");
            if (fxml == null) throw new IllegalStateException("No se encontro main.fxml");
            FXMLLoader loader = new FXMLLoader(fxml);
            Scene scene = new Scene(loader.load(), 1200, 800);
            URL css = getClass().getResource("styles.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Salsiao - Sistema Principal");
            stage.show();
        } catch (Exception e) {
            System.out.println("Error volviendo al menú: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ================= MODELOS =================
    @FunctionalInterface
    interface ResultSetMapper<T> { T map(ResultSet rs) throws SQLException; }

    public static class Empleado {
        private int id; private String cedula, nombre, apellido, telefono, email, direccion, cargo, departamento, salario, estado;
        public Empleado(int id, String cedula, String nombre, String apellido, String telefono, String email, String direccion, String cargo, String departamento, String salario, String estado) {
            this.id = id; this.cedula = cedula; this.nombre = nombre; this.apellido = apellido; this.telefono = telefono; this.email = email; this.direccion = direccion; this.cargo = cargo; this.departamento = departamento; this.salario = salario; this.estado = estado;
        }
        public int getId() { return id; }
        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getApellido() { return apellido; }
        public String getTelefono() { return telefono; }
        public String getEmail() { return email; }
        public String getDireccion() { return direccion; }
        public String getCargo() { return cargo; }
        public String getDepartamento() { return departamento; }
        public String getSalario() { return salario; }
        public String getEstado() { return estado; }
        @Override public String toString() { return nombre + " " + apellido; }
    }

    public static class Documento { private int id; private String empleado, tipo, estado; public Documento(int id, String empleado, String tipo, String estado) { this.id=id; this.empleado=empleado; this.tipo=tipo; this.estado=estado; } public int getId() { return id; } public String getEmpleado() { return empleado; } public String getTipo() { return tipo; } public String getEstado() { return estado; } }
    public static class Contratacion { private int id; private String empleado, estado; public Contratacion(int id, String empleado, String estado) { this.id=id; this.empleado=empleado; this.estado=estado; } public int getId() { return id; } public String getEmpleado() { return empleado; } public String getEstado() { return estado; } }
    public static class Capacitacion { private int id; private String empleado, estado; public Capacitacion(int id, String empleado, String estado) { this.id=id; this.empleado=empleado; this.estado=estado; } public int getId() { return id; } public String getEmpleado() { return empleado; } public String getEstado() { return estado; } }
    public static class Conflicto { private int id; private String empleado, tipo, estado; public Conflicto(int id, String empleado, String tipo, String estado) { this.id=id; this.empleado=empleado; this.tipo=tipo; this.estado=estado; } public int getId() { return id; } public String getEmpleado() { return empleado; } public String getTipo() { return tipo; } public String getEstado() { return estado; } }
}