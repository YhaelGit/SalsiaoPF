package org.example.salsiaopf;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class MantenimientoController implements Initializable {

    @FXML private ImageView logoImage;

    @FXML private ToggleButton segLocal;
    @FXML private ToggleButton segArea;
    @FXML private ToggleButton segEquipo;
    @FXML private ToggleButton segTecnico;
    @FXML private ToggleButton segConserje;
    @FXML private ToggleButton segLimpieza;
    @FXML private ToggleButton segMantenimiento;

    @FXML private VBox viewLocal;
    @FXML private VBox viewArea;
    @FXML private VBox viewEquipo;
    @FXML private VBox viewTecnico;
    @FXML private VBox viewConserje;
    @FXML private VBox viewLimpieza;
    @FXML private VBox viewMantenimiento;


    @FXML private TextField txtLocalId, txtLocalNombre, txtLocalDireccion;
    @FXML private ComboBox<String> cbLocalEstado;
    @FXML private TableView<Local> tablaLocales;


    @FXML private TextField txtAreaId, txtAreaNombre, txtAreaTipo, txtAreaTamano;
    @FXML private ComboBox<String> cbAreaEstado;
    @FXML private TableView<Area> tablaAreas;

    @FXML private TextField txtEquipoId, txtEquipoNombre, txtEquipoTipo, txtEquipoMarca, txtEquipoUbicacion;
    @FXML private ComboBox<String> cbEquipoEstado;
    @FXML private DatePicker dpEquipoFechaCompra;
    @FXML private TableView<Equipo> tablaEquipos;

    @FXML private TextField txtTecnicoId, txtTecnicoNombre, txtTecnicoEmpresa, txtTecnicoEspecialidad, txtTecnicoTelefono;
    @FXML private TableView<Tecnico> tablaTecnicos;

    private ObservableList<Local> listaLocales = FXCollections.observableArrayList();
    private ObservableList<Area> listaAreas = FXCollections.observableArrayList();
    private ObservableList<Equipo> listaEquipos = FXCollections.observableArrayList();
    private ObservableList<Tecnico> listaTecnicos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarLogo();
        cargarCombos();
        configurarTablas();
        segLocal.setSelected(true);
        showOnly(viewLocal);
        cargarLocales();
    }

    private void cargarCombos() {
        cbLocalEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbAreaEstado.setItems(FXCollections.observableArrayList("Activo", "Inactivo"));
        cbEquipoEstado.setItems(FXCollections.observableArrayList("Operativo", "Dañado", "Inactivo"));
    }

    private void configurarTablas() {
        // Locales
        tablaLocales.setItems(listaLocales);
        tablaLocales.getSelectionModel().selectedItemProperty().addListener((obs, old, n) -> { if (n != null) llenarLocal(n); });
        // Areas
        tablaAreas.setItems(listaAreas);
        // Equipos
        tablaEquipos.setItems(listaEquipos);
        // Tecnicos
        tablaTecnicos.setItems(listaTecnicos);
    }

    private void llenarLocal(Local l) { txtLocalId.setText(String.valueOf(l.getId())); txtLocalNombre.setText(l.getNombre()); txtLocalDireccion.setText(l.getDireccion()); cbLocalEstado.setValue(l.getEstado()); }

    private void cargarLogo() {
        try {
            var stream = getClass().getResourceAsStream("/imagenes/logo-salsiao.jpeg");
            if (stream != null) {
                logoImage.setImage(new Image(stream));
                logoImage.setClip(new javafx.scene.shape.Circle(35, 35, 35));
            }
        } catch (Exception e) { System.out.println("Error cargando imagen: " + e.getMessage()); }
    }

    private void showOnly(VBox target) {
        VBox[] all = {viewLocal, viewArea, viewEquipo, viewTecnico, viewConserje, viewLimpieza, viewMantenimiento};
        for (VBox v : all) { boolean active = (v == target); v.setVisible(active); v.setManaged(active); }
    }

    @FXML private void showLocal() { showOnly(viewLocal); cargarLocales(); }
    @FXML private void showArea() { showOnly(viewArea); cargarAreas(); }
    @FXML private void showEquipo() { showOnly(viewEquipo); cargarEquipos(); }
    @FXML private void showTecnico() { showOnly(viewTecnico); cargarTecnicos(); }
    @FXML private void showConserje() { showOnly(viewConserje); }
    @FXML private void showLimpieza() { showOnly(viewLimpieza); }
    @FXML private void showMantenimiento() { showOnly(viewMantenimiento); }

    @FXML private void guardarLocal() {
        if (txtLocalNombre.getText().trim().isEmpty()) { mostrarAlerta("Validación", "Nombre obligatorio", Alert.AlertType.WARNING); return; }
        crudBasico("Locales", "id_local", txtLocalId.getText(),
            new String[]{"nombre","direccion","estado"},
            new String[]{txtLocalNombre.getText(), txtLocalDireccion.getText(), cbLocalEstado.getValue()},
            this::cargarLocales);
    }
    @FXML private void eliminarLocal() { eliminarRegistro("Locales", "id_local", txtLocalId.getText(), this::cargarLocales); }
    @FXML private void limpiarLocal() { txtLocalId.clear(); txtLocalNombre.clear(); txtLocalDireccion.clear(); cbLocalEstado.setValue(null); }

    private void cargarLocales() { cargarTabla("SELECT * FROM Locales ORDER BY nombre", rs -> new Local(rs.getInt("id_local"), rs.getString("nombre"), rs.getString("direccion"), rs.getString("estado")), listaLocales); }

    @FXML private void guardarArea() {
        if (txtAreaNombre.getText().trim().isEmpty()) { mostrarAlerta("Validación", "Nombre obligatorio", Alert.AlertType.WARNING); return; }
        crudBasico("Areas", "id_area", txtAreaId.getText(),
            new String[]{"nombre","tipo","tamano","estado"},
            new String[]{txtAreaNombre.getText(), txtAreaTipo.getText(), txtAreaTamano.getText(), cbAreaEstado.getValue()},
            this::cargarAreas);
    }
    @FXML private void eliminarArea() { eliminarRegistro("Areas", "id_area", txtAreaId.getText(), this::cargarAreas); }
    @FXML private void limpiarArea() { txtAreaId.clear(); txtAreaNombre.clear(); txtAreaTipo.clear(); txtAreaTamano.clear(); cbAreaEstado.setValue(null); }

    private void cargarAreas() { cargarTabla("SELECT * FROM Areas ORDER BY nombre", rs -> new Area(rs.getInt("id_area"), rs.getString("nombre"), rs.getString("tipo"), rs.getString("estado")), listaAreas); }

    @FXML private void guardarEquipo() {
        if (txtEquipoNombre.getText().trim().isEmpty()) { mostrarAlerta("Validación", "Nombre obligatorio", Alert.AlertType.WARNING); return; }
        crudBasico("Equipos", "id_equipo", txtEquipoId.getText(),
            new String[]{"nombre","tipo","marca","estado","ubicacion"},
            new String[]{txtEquipoNombre.getText(), txtEquipoTipo.getText(), txtEquipoMarca.getText(), cbEquipoEstado.getValue(), txtEquipoUbicacion.getText()},
            this::cargarEquipos);
    }
    @FXML private void eliminarEquipo() { eliminarRegistro("Equipos", "id_equipo", txtEquipoId.getText(), this::cargarEquipos); }
    @FXML private void limpiarEquipo() { txtEquipoId.clear(); txtEquipoNombre.clear(); txtEquipoTipo.clear(); txtEquipoMarca.clear(); txtEquipoUbicacion.clear(); cbEquipoEstado.setValue(null); dpEquipoFechaCompra.setValue(null); }

    private void cargarEquipos() { cargarTabla("SELECT * FROM Equipos ORDER BY nombre", rs -> new Equipo(rs.getInt("id_equipo"), rs.getString("nombre"), rs.getString("tipo"), rs.getString("marca"), rs.getString("estado")), listaEquipos); }

    @FXML private void guardarTecnico() {
        if (txtTecnicoNombre.getText().trim().isEmpty()) { mostrarAlerta("Validación", "Nombre obligatorio", Alert.AlertType.WARNING); return; }
        crudBasico("Tecnicos", "id_tecnico", txtTecnicoId.getText(),
            new String[]{"nombre","empresa","especialidad","telefono"},
            new String[]{txtTecnicoNombre.getText(), txtTecnicoEmpresa.getText(), txtTecnicoEspecialidad.getText(), txtTecnicoTelefono.getText()},
            this::cargarTecnicos);
    }
    @FXML private void eliminarTecnico() { eliminarRegistro("Tecnicos", "id_tecnico", txtTecnicoId.getText(), this::cargarTecnicos); }
    @FXML private void limpiarTecnico() { txtTecnicoId.clear(); txtTecnicoNombre.clear(); txtTecnicoEmpresa.clear(); txtTecnicoEspecialidad.clear(); txtTecnicoTelefono.clear(); }

    private void cargarTecnicos() { cargarTabla("SELECT * FROM Tecnicos ORDER BY nombre", rs -> new Tecnico(rs.getInt("id_tecnico"), rs.getString("nombre"), rs.getString("empresa"), rs.getString("especialidad"), rs.getString("telefono")), listaTecnicos); }

    private <T> void cargarTabla(String sql, ResultSetMapper<T> mapper, ObservableList<T> lista) {
        lista.clear();
        try (Connection conn = ConexionBD.conectar()) { if (conn == null) return;
            Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql);
            while (rs.next()) lista.add(mapper.map(rs));
        } catch (SQLException e) { System.out.println("Error: " + e.getMessage()); }
    }

    private void crudBasico(String tabla, String campoId, String idTxt, String[] campos, String[] valores, Runnable callback) {
        try (Connection conn = ConexionBD.conectar()) { if (conn == null) return;
            String sql = (idTxt == null || idTxt.trim().isEmpty())
                ? "INSERT INTO " + tabla + " (" + String.join(",", campos) + ") VALUES (" + String.join(",", java.util.Collections.nCopies(campos.length, "?")) + ")"
                : "UPDATE " + tabla + " SET " + String.join("=?," , campos) + "=? WHERE " + campoId + "=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0; i < valores.length; i++) ps.setString(i + 1, valores[i]);
            if (idTxt != null && !idTxt.trim().isEmpty()) ps.setInt(valores.length + 1, Integer.parseInt(idTxt));
            ps.executeUpdate();
            mostrarAlerta("Éxito", "Registro guardado", Alert.AlertType.INFORMATION);
            callback.run();
        } catch (SQLException e) { mostrarAlerta("Error", e.getMessage(), Alert.AlertType.ERROR); }
    }

    private void eliminarRegistro(String tabla, String campoId, String idTxt, Runnable callback) {
        if (idTxt == null || idTxt.trim().isEmpty()) { mostrarAlerta("Validación", "Seleccione registro", Alert.AlertType.WARNING); return; }
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

    @FXML private void volverMenu(javafx.event.ActionEvent event) {
        try { FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene); stage.setTitle("Salsiao - Sistema Principal"); stage.show();
        } catch (Exception e) { System.out.println("Error volviendo al menú: " + e.getMessage()); }
    }

    @FunctionalInterface interface ResultSetMapper<T> { T map(ResultSet rs) throws SQLException; }

    public static class Local { private int id; private String nombre, direccion, estado; public Local(int id, String nombre, String direccion, String estado) { this.id=id; this.nombre=nombre; this.direccion=direccion; this.estado=estado; } public int getId() { return id; } public String getNombre() { return nombre; } public String getDireccion() { return direccion; } public String getEstado() { return estado; } }
    public static class Area { private int id; private String nombre, tipo, estado; public Area(int id, String nombre, String tipo, String estado) { this.id=id; this.nombre=nombre; this.tipo=tipo; this.estado=estado; } public int getId() { return id; } public String getNombre() { return nombre; } public String getTipo() { return tipo; } public String getEstado() { return estado; } }
    public static class Equipo { private int id; private String nombre, tipo, marca, estado; public Equipo(int id, String nombre, String tipo, String marca, String estado) { this.id=id; this.nombre=nombre; this.tipo=tipo; this.marca=marca; this.estado=estado; } public int getId() { return id; } public String getNombre() { return nombre; } public String getTipo() { return tipo; } public String getMarca() { return marca; } public String getEstado() { return estado; } }
    public static class Tecnico { private int id; private String nombre, empresa, especialidad, telefono; public Tecnico(int id, String nombre, String empresa, String especialidad, String telefono) { this.id=id; this.nombre=nombre; this.empresa=empresa; this.especialidad=especialidad; this.telefono=telefono; } public int getId() { return id; } public String getNombre() { return nombre; } public String getEmpresa() { return empresa; } public String getEspecialidad() { return especialidad; } public String getTelefono() { return telefono; } }
}